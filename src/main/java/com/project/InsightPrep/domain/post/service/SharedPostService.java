package com.project.InsightPrep.domain.post.service;

import com.project.InsightPrep.domain.post.dto.PostRequest.Create;
import com.project.InsightPrep.domain.post.dto.PostResponse.PostDetailDto;

public interface SharedPostService {
    Long createPost(Create req);

    PostDetailDto getPostDetail(long postId);

    void resolve(long postId);
}
