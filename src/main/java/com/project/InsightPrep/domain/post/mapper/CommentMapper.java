package com.project.InsightPrep.domain.post.mapper;

import com.project.InsightPrep.domain.post.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper {

    void insertComment(Comment comment);
}
