package com.project.InsightPrep.domain.question.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.project.InsightPrep.domain.question.entity.ItemType;
import com.project.InsightPrep.domain.question.entity.RecentPromptFilter;
import com.project.InsightPrep.domain.question.mapper.RecentPromptFilterMapper;
import com.project.InsightPrep.domain.question.repository.RecentPromptFilterRepository;
import java.time.Duration;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

@ExtendWith(MockitoExtension.class)
class RecentPromptFilterServiceImplTest {

    @Mock StringRedisTemplate redis;
    @Mock RecentPromptFilterMapper recentMapper;
    @Mock
    RecentPromptFilterRepository recentPromptFilterRepository;
    @Mock ZSetOperations<String, String> zset;

    @InjectMocks RecentPromptFilterServiceImpl service;

    private final long memberId = 42L;
    private final String category = "Java";
    private final ItemType type = ItemType.TOPIC;
    private final String value = "volatile";

    private String key;

    @BeforeEach
    void setUp() {
        when(redis.opsForZSet()).thenReturn(zset);
        key = String.format("rp:%d:%s:%s", memberId, category, type.name());
    }

    @Nested
    class RecordTests {

        @Test
        @DisplayName("record() - 정상: DB insert + Redis ZSET add + TTL + 오래된 항목 trim")
        void record_ok() {
            // given: 존재하지 않음 → save() 호출 예상
            when(recentPromptFilterRepository.existsByMemberIdAndCategoryAndItemTypeAndItemValue(
                    memberId, category, type, value
            )).thenReturn(false);

            // size가 15라고 가정 → MAX_SIZE(10) 초과 → 0..4 삭제 호출
            when(zset.size(key)).thenReturn(15L);

            // when
            service.record(memberId, category, type, value);

            // then: existsBy + save 호출 검증
            verify(recentPromptFilterRepository).existsByMemberIdAndCategoryAndItemTypeAndItemValue(memberId, category, type, value);
            ArgumentCaptor<RecentPromptFilter> cap = ArgumentCaptor.forClass(RecentPromptFilter.class);
            verify(recentPromptFilterRepository).save(cap.capture());

            RecentPromptFilter saved = cap.getValue();
            assertThat(saved.getMemberId()).isEqualTo(memberId);
            assertThat(saved.getCategory()).isEqualTo(category);
            assertThat(saved.getItemType()).isEqualTo(type);
            assertThat(saved.getItemValue()).isEqualTo(value);

            // Redis: ZADD + size + removeRange + expire
            verify(zset).add(eq(key), eq(value), anyDouble());
            verify(zset).size(key);
            // size=15, MAX=10 → 0..(15-10-1)=0..4 제거
            verify(zset).removeRange(key, 0, 4);
            verify(redis).expire(eq(key), eq(RecentPromptFilterServiceImpl.TTL));
        }

        @Test
        @DisplayName("record() - 이미 존재하는 경우 save() 호출 안함, Redis는 정상 갱신")
        void record_alreadyExists_noSave() {
            // given: 이미 존재 → existsBy == true → save() 안함
            when(recentPromptFilterRepository.existsByMemberIdAndCategoryAndItemTypeAndItemValue(
                    memberId, category, type, value
            )).thenReturn(true);

            when(zset.size(key)).thenReturn(5L); // trim 없음

            // when
            service.record(memberId, category, type, value);

            // then: DB 저장은 안됨
            verify(recentPromptFilterRepository).existsByMemberIdAndCategoryAndItemTypeAndItemValue(memberId, category, type, value);
            verify(recentPromptFilterRepository, never()).save(any());

            // Redis는 정상 갱신됨
            verify(zset).add(eq(key), eq(value), anyDouble());
            verify(redis).expire(eq(key), eq(RecentPromptFilterServiceImpl.TTL));
            verify(zset, never()).removeRange(anyString(), anyLong(), anyLong());
        }
    }

    @Nested
    class GetRecentTests {

        @Test
        @DisplayName("getRecent() - 캐시 HIT: Redis ZSET에서 최신 N개 반환, Repository 호출 안 함")
        void getRecent_cacheHit() {
            // given
            Set<String> cached = new LinkedHashSet<>(List.of("a", "b", "c"));
            when(zset.reverseRange(key, 0, 4)).thenReturn(cached);

            // when
            List<String> res = service.getRecent(memberId, category, type, 5);

            // then
            assertThat(res).containsExactly("a", "b", "c");
            verify(recentPromptFilterRepository, never()).findByMemberIdAndCategoryAndItemTypeOrderByCreatedAtDesc(anyLong(), anyString(), any(), any(
                    Pageable.class));
            verify(redis, never()).expire(anyString(), any(Duration.class)); // 캐시 HIT 시 expire 갱신 안 함(구현 그대로 검증)
        }

        @Test
        @DisplayName("getRecent() - 캐시 MISS: DB fallback 후 Redis warm-up 및 반환")
        void getRecent_cacheMiss_dbFallbackAndWarmup() {
            // given: 캐시 미스
            when(zset.reverseRange(key, 0, 9)).thenReturn(Collections.emptySet());

            // DB에서 반환될 mock 엔티티 리스트
            List<RecentPromptFilter> fromDbEntities = List.of(
                    RecentPromptFilter.builder().itemValue("t1").build(),
                    RecentPromptFilter.builder().itemValue("t2").build(),
                    RecentPromptFilter.builder().itemValue("t3").build()
            );

            when(recentPromptFilterRepository.findByMemberIdAndCategoryAndItemTypeOrderByCreatedAtDesc(eq(memberId), eq(category), eq(type), any(Pageable.class)))
                    .thenReturn(fromDbEntities);

            // when
            List<String> res = service.getRecent(memberId, category, type, 10);

            // then
            assertThat(res).containsExactly("t1", "t2", "t3");

            // DB 호출 검증
            verify(recentPromptFilterRepository).findByMemberIdAndCategoryAndItemTypeOrderByCreatedAtDesc(
                    eq(memberId), eq(category), eq(type), any(Pageable.class)
            );

            // Redis warm-up: add 3번 + expire
            verify(zset, times(fromDbEntities.size())).add(eq(key), anyString(), anyDouble());
            verify(redis).expire(eq(key), eq(RecentPromptFilterServiceImpl.TTL));
        }
    }
}