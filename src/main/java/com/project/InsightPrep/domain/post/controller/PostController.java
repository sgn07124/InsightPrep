package com.project.InsightPrep.domain.post.controller;

import com.project.InsightPrep.domain.post.controller.docs.PostControllerDocs;
import com.project.InsightPrep.domain.post.dto.PostRequest;
import com.project.InsightPrep.domain.post.dto.PostResponse;
import com.project.InsightPrep.domain.post.dto.PostResponse.Created;
import com.project.InsightPrep.domain.post.service.SharedPostService;
import com.project.InsightPrep.global.common.response.ApiResponse;
import com.project.InsightPrep.global.common.response.code.ApiSuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
public class PostController implements PostControllerDocs {

    private final SharedPostService sharedPostService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Created>> create(@RequestBody @Valid PostRequest.Create req) {
        Long postId = sharedPostService.createPost(req);
        return ResponseEntity.ok(ApiResponse.of(ApiSuccessCode.CREATE_POST_SUCCESS, new PostResponse.Created(postId))
        );
    }
}
