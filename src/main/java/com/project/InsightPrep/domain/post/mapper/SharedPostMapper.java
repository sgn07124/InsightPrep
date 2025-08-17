package com.project.InsightPrep.domain.post.mapper;

import com.project.InsightPrep.domain.post.dto.PostResponse.PostDetailDto;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

@Mapper
public interface SharedPostMapper {

    int insertSharedPost(@Param("title") String title,
                         @Param("content") String content,
                         @Param("answerId") Long answerId,
                         @Param("memberId") Long memberId,
                         @Param("status") String status);

    Long lastInsertedId();

    boolean existsByAnswerId(@Param("answerId") Long answerId);

    PostDetailDto findPostDetailById(@Param("postId") long postId);
}
