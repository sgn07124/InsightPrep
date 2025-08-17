package com.project.InsightPrep.domain.post.service;

import com.project.InsightPrep.domain.post.dto.PostRequest.Create;

public interface SharedPostService {
    Long createPost(Create req);
}
