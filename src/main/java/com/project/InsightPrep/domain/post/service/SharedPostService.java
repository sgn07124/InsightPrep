package com.project.InsightPrep.domain.post.service;

import com.project.InsightPrep.domain.post.dto.PostRequest.Create;
import com.project.InsightPrep.domain.post.dto.PostResponse.PostDetailDto;
import com.project.InsightPrep.domain.post.dto.PostResponse.PostListItemDto;
import com.project.InsightPrep.domain.question.dto.response.PageResponse;

public interface SharedPostService {
    Long createPost(Create req);

    PostDetailDto getPostDetail(long postId);

    void resolve(long postId);

    PageResponse<PostListItemDto> getPosts(int page, int size);
}
