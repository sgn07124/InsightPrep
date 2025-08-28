package com.project.InsightPrep.domain.question.mapper;

import com.project.InsightPrep.domain.question.entity.ItemType;
import com.project.InsightPrep.domain.question.entity.RecentPromptFilter;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RecentPromptFilterMapper {
    void insert(RecentPromptFilter recentPromptFilter);

    List<String> findTopNByUserCategoryType(
            @Param("memberId") long memberId,
            @Param("category") String category,
            @Param("type")ItemType type,
            @Param("limit") int limit);
}
