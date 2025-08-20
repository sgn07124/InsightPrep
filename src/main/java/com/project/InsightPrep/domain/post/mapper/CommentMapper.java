package com.project.InsightPrep.domain.post.mapper;

import com.project.InsightPrep.domain.post.dto.CommentResponse.CommentListItem;
import com.project.InsightPrep.domain.post.dto.CommentResponse.CommentRow;
import com.project.InsightPrep.domain.post.entity.Comment;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CommentMapper {

    void insertComment(Comment comment);

    Comment findById(@Param("id") long id);

    int updateContent(@Param("id") long id, @Param("memberId") long memberId, @Param("content") String content);

    int deleteByIdAndMember(@Param("id") long id, @Param("memberId") long memberId);

    CommentRow findRowById(@Param("id") long id);

    List<CommentListItem> findByPostPaged(@Param("postId") long postId, @Param("limit") int limit, @Param("offset") int offset);

    long countByPost(@Param("postId") long postId);
}
