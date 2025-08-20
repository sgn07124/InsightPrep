package com.project.InsightPrep.domain.post.service;

import com.project.InsightPrep.domain.post.dto.CommentRequest.CreateDto;
import com.project.InsightPrep.domain.post.dto.CommentRequest.UpdateDto;
import com.project.InsightPrep.domain.post.dto.CommentResponse.CommentListItem;
import com.project.InsightPrep.domain.post.dto.CommentResponse.CommentRes;
import com.project.InsightPrep.domain.question.dto.response.PageResponse;

public interface CommentService {
    CommentRes createComment(long postId, CreateDto req);

    void updateComment(long postId, long commentId, UpdateDto req);

    void deleteComment(long postId, long commentId);

    PageResponse<CommentListItem> getComments(long postId, int page, int size);
}
