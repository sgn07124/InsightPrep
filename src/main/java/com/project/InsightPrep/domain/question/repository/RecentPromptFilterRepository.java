package com.project.InsightPrep.domain.question.repository;

import com.project.InsightPrep.domain.question.entity.ItemType;
import com.project.InsightPrep.domain.question.entity.RecentPromptFilter;
import jakarta.persistence.QueryHint;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;

public interface RecentPromptFilterRepository extends JpaRepository<RecentPromptFilter, Long> {

    @QueryHints(@QueryHint(name = "org.hibernate.readOnly", value = "true"))
    List<RecentPromptFilter> findByMemberIdAndCategoryAndItemTypeOrderByCreatedAtDesc(
            Long memberId, String category, ItemType itemType, Pageable pageable);

    /**중복 방지를 위해 이미 존재하는지 확인 (ON CONFLICT 대체용)*/
    boolean existsByMemberIdAndCategoryAndItemTypeAndItemValue(
            Long memberId,
            String category,
            ItemType itemType,
            String itemValue
    );
}
