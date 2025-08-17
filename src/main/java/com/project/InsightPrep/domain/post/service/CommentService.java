package com.project.InsightPrep.domain.post.service;

import com.project.InsightPrep.domain.post.dto.CommentRequest.CreateDto;
import com.project.InsightPrep.domain.post.dto.CommentResponse.CommentRes;

public interface CommentService {
    CommentRes createComment(long postId, CreateDto req);
}
