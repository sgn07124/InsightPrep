package com.project.InsightPrep.domain.post.controller;

import com.project.InsightPrep.domain.post.controller.docs.PostControllerDocs;
import com.project.InsightPrep.domain.post.dto.CommentRequest;
import com.project.InsightPrep.domain.post.dto.CommentResponse.CommentRes;
import com.project.InsightPrep.domain.post.dto.PostRequest;
import com.project.InsightPrep.domain.post.dto.PostResponse;
import com.project.InsightPrep.domain.post.dto.PostResponse.Created;
import com.project.InsightPrep.domain.post.dto.PostResponse.PostDetailDto;
import com.project.InsightPrep.domain.post.dto.PostResponse.PostListItemDto;
import com.project.InsightPrep.domain.post.service.CommentService;
import com.project.InsightPrep.domain.post.service.SharedPostService;
import com.project.InsightPrep.domain.question.dto.response.PageResponse;
import com.project.InsightPrep.global.common.response.ApiResponse;
import com.project.InsightPrep.global.common.response.code.ApiSuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
public class PostController implements PostControllerDocs {

    private final SharedPostService sharedPostService;
    private final CommentService commentService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Created>> create(@RequestBody @Valid PostRequest.Create req) {
        Long postId = sharedPostService.createPost(req);
        return ResponseEntity.ok(ApiResponse.of(ApiSuccessCode.CREATE_POST_SUCCESS, new PostResponse.Created(postId)));
    }

    @GetMapping("/{postId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<PostDetailDto>> getPost(@PathVariable long postId) {
        PostDetailDto dto = sharedPostService.getPostDetail(postId);
        return ResponseEntity.ok(ApiResponse.of(ApiSuccessCode.GET_POST_SUCCESS, dto));
    }

    @PatchMapping("/{postId}/resolve")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Void>> resolve(@PathVariable long postId) {
        sharedPostService.resolve(postId);
        return ResponseEntity.ok(ApiResponse.of(ApiSuccessCode.SUCCESS));
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<PageResponse<PostListItemDto>>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageResponse<PostListItemDto> body = sharedPostService.getPosts(page, size);
        return ResponseEntity.ok(ApiResponse.of(ApiSuccessCode.SUCCESS, body));
    }

    @PostMapping("/{postId}/comments")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<CommentRes>> createComment(@PathVariable long postId, @RequestBody @Valid CommentRequest.CreateDto req) {
        CommentRes res = commentService.createComment(postId, req);
        return ResponseEntity.ok(ApiResponse.of(ApiSuccessCode.SUCCESS, res));
    }

    @PutMapping("/{postId}/comments/{commentId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Void>> updateComment(@PathVariable long postId, @PathVariable long commentId, @RequestBody @Valid CommentRequest.UpdateDto req) {
        commentService.updateComment(postId, commentId, req);
        return ResponseEntity.ok(ApiResponse.of(ApiSuccessCode.SUCCESS));
    }

    @DeleteMapping("/{postId}/comments/{commentId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable long postId, @PathVariable long commentId) {
        commentService.deleteComment(postId, commentId);
        return ResponseEntity.ok(ApiResponse.of(ApiSuccessCode.SUCCESS));
    }
}
