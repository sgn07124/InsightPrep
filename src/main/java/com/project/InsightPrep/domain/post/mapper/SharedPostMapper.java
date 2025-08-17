package com.project.InsightPrep.domain.post.mapper;

import com.project.InsightPrep.domain.post.dto.PostRequest.PostOwnerStatusDto;
import com.project.InsightPrep.domain.post.dto.PostResponse.PostDetailDto;
import com.project.InsightPrep.domain.post.dto.PostResponse.PostListItemDto;
import java.util.List;
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

    PostOwnerStatusDto findOwnerAndStatus(@Param("postId") long postId);

    int updateStatusToResolved(@Param("postId") long postId);

    List<PostListItemDto> findSharedPostsPaged(@Param("limit") int limit, @Param("offset") int offset);

    long countSharedPosts();
}
