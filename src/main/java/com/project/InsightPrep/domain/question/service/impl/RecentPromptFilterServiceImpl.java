package com.project.InsightPrep.domain.question.service.impl;

import com.project.InsightPrep.domain.question.entity.ItemType;
import com.project.InsightPrep.domain.question.entity.RecentPromptFilter;
import com.project.InsightPrep.domain.question.mapper.RecentPromptFilterMapper;
import com.project.InsightPrep.domain.question.service.RecentPromptFilterService;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class RecentPromptFilterServiceImpl implements RecentPromptFilterService {

    private final StringRedisTemplate redis;
    private final RecentPromptFilterMapper recentMapper;
    private static final String KEY_FMT = "rp:%d:%s:%s";  // memberId, category, type
    public static final int MAX_SIZE = 10;
    public static final Duration TTL = Duration.ofDays(14); // 만료일

    @Override
    @Transactional
    public void record(long memberId, String category, ItemType type, String value) {
        // DB 영구 저장 (unique 제약 조건으로 중복 방지)
        RecentPromptFilter recentPromptFilter = RecentPromptFilter.builder()
                .memberId(memberId)
                .category(category)
                .itemType(type)
                .itemValue(value)
                .build();
        try {
            recentMapper.insert(recentPromptFilter);
        } catch (DataIntegrityViolationException ignore) {
            // 유니크 제약 충돌은 무시 (이미 기록된 값)
        }

        // redis 캐시 (최근 10개 유지)
        String key = key(memberId, category, type);
        double score = System.currentTimeMillis();
        redis.opsForZSet().add(key, value, score);

        // 오래된 것 제거
        Long size = redis.opsForZSet().size(key);  // ZSet: 낮은 rank가 오래된 것
        if (size != null && size > MAX_SIZE) {
            redis.opsForZSet().removeRange(key, 0, size - MAX_SIZE - 1);
        }

        redis.expire(key, TTL);  // TTL 적용
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getRecent(long memberId, String category, ItemType type, int limit) {
        String key = key(memberId, category, type);

        // 최신순 상위 N
        Set<String> z = redis.opsForZSet().reverseRange(key, 0, Math.max(0, limit - 1));
        if (z != null && !z.isEmpty()) {
            return new ArrayList<>(z);
        }

        // 캐시 미스 → DB fallback (최근 10개)
        List<String> fromDb = recentMapper.findTopNByUserCategoryType(memberId, category, type, limit);
        for (int i = 0; i < fromDb.size(); i++) {
            redis.opsForZSet().add(key, fromDb.get(i), System.currentTimeMillis() + i);
        }
        redis.expire(key, TTL);
        return fromDb;
    }

    private String key(long userId, String category, ItemType type) {
        return String.format(KEY_FMT, userId, category, type.name());
    }
}
