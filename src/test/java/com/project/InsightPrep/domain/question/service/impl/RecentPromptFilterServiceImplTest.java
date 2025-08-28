package com.project.InsightPrep.domain.question.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.project.InsightPrep.domain.question.entity.ItemType;
import com.project.InsightPrep.domain.question.entity.RecentPromptFilter;
import com.project.InsightPrep.domain.question.mapper.RecentPromptFilterMapper;
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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

@ExtendWith(MockitoExtension.class)
class RecentPromptFilterServiceImplTest {

    @Mock StringRedisTemplate redis;
    @Mock RecentPromptFilterMapper recentMapper;
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
            // given: DB insert 정상
            doNothing().when(recentMapper).insert(any(RecentPromptFilter.class));
            // size가 15라고 가정 → MAX_SIZE(10) 초과 → 0..4 삭제 호출
            when(zset.size(key)).thenReturn(15L);

            // when
            service.record(memberId, category, type, value);

            // then: DB insert 호출
            ArgumentCaptor<RecentPromptFilter> cap = ArgumentCaptor.forClass(RecentPromptFilter.class);
            verify(recentMapper).insert(cap.capture());
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
        @DisplayName("record() - DB 유니크 충돌 시 예외 무시하고 Redis는 정상 갱신")
        void record_duplicate_ignoreDbError() {
            // given: unique 제약 충돌 유발
            doThrow(new DataIntegrityViolationException("dup")).when(recentMapper).insert(any(RecentPromptFilter.class));
            when(zset.size(key)).thenReturn(1L); // trim 안 일어나도록

            // when/then
            assertDoesNotThrow(() -> service.record(memberId, category, type, value));

            // DB insert 시도는 했지만, 예외는 흡수
            verify(recentMapper).insert(any(RecentPromptFilter.class));

            // Redis는 정상 갱신
            verify(zset).add(eq(key), eq(value), anyDouble());
            verify(redis).expire(eq(key), eq(RecentPromptFilterServiceImpl.TTL));
            // size가 MAX 이하 → trim 안 함
            verify(zset, never()).removeRange(anyString(), anyLong(), anyLong());
        }
    }

    @Nested
    class GetRecentTests {

        @Test
        @DisplayName("getRecent() - 캐시 HIT: Redis ZSET에서 최신 N개 반환, DAO 호출 안 함")
        void getRecent_cacheHit() {
            // given
            Set<String> cached = new LinkedHashSet<>(List.of("a", "b", "c"));
            when(zset.reverseRange(key, 0, 4)).thenReturn(cached);

            // when
            List<String> res = service.getRecent(memberId, category, type, 5);

            // then
            assertThat(res).containsExactly("a", "b", "c");
            verify(recentMapper, never()).findTopNByUserCategoryType(anyLong(), anyString(), any(), anyInt());
            verify(redis, never()).expire(anyString(), any(Duration.class)); // 캐시 HIT 시 expire 갱신 안 함(구현 그대로 검증)
        }

        @Test
        @DisplayName("getRecent() - 캐시 MISS: DB fallback 후 Redis warm-up 및 반환")
        void getRecent_cacheMiss_dbFallbackAndWarmup() {
            // given: 캐시 미스
            when(zset.reverseRange(key, 0, 9)).thenReturn(Collections.emptySet());

            List<String> fromDb = List.of("t1", "t2", "t3");
            when(recentMapper.findTopNByUserCategoryType(memberId, category, type, 10))
                    .thenReturn(fromDb);

            // when
            List<String> res = service.getRecent(memberId, category, type, 10);

            // then
            assertThat(res).containsExactlyElementsOf(fromDb);

            // DB 호출
            verify(recentMapper).findTopNByUserCategoryType(memberId, category, type, 10);

            // Redis warm-up: add 3번 + expire
            verify(zset, times(fromDb.size())).add(eq(key), anyString(), anyDouble());
            verify(redis).expire(eq(key), eq(RecentPromptFilterServiceImpl.TTL));
        }
    }
}