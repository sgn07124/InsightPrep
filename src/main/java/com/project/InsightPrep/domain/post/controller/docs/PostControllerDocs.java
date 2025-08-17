package com.project.InsightPrep.domain.post.controller.docs;

import com.project.InsightPrep.domain.post.dto.CommentRequest;
import com.project.InsightPrep.domain.post.dto.CommentResponse.CommentRes;
import com.project.InsightPrep.domain.post.dto.PostRequest;
import com.project.InsightPrep.domain.post.dto.PostResponse.Created;
import com.project.InsightPrep.domain.post.dto.PostResponse.PostDetailDto;
import com.project.InsightPrep.domain.post.dto.PostResponse.PostListItemDto;
import com.project.InsightPrep.domain.question.dto.response.PageResponse;
import com.project.InsightPrep.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Discussions", description = "Discussions 관련 API")
public interface PostControllerDocs {

    @Operation(summary = "토론 글 등록", description = "특정 질문에 대하여 글을 등록합니다.")
    public ResponseEntity<ApiResponse<Created>> create(@RequestBody @Valid PostRequest.Create req);

    @Operation(summary = "토론 글 조회", description = "글을 조회합니다.")
    public ResponseEntity<ApiResponse<PostDetailDto>> getPost(@PathVariable long postId);

    @Operation(summary = "본인 글 상태 변경", description = "본인이 작성한 글의 상태를 해결 완료 상태로 변경합니다.")
    public ResponseEntity<ApiResponse<Void>> resolve(@PathVariable long postId);

    @Operation(summary = "글 리스트 조회", description = "사용자들이 작성한 글들을 조회합니다.")
    public ResponseEntity<ApiResponse<PageResponse<PostListItemDto>>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    );

    @Operation(summary = "댓글 작성", description = "특정 글에 댓글을 작성합니다.")
    public ResponseEntity<ApiResponse<CommentRes>> createComment(@PathVariable long postId, @RequestBody @Valid CommentRequest.CreateDto req);

    @Operation(summary = "댓글 수정", description = "본인의 댓글을 수정합니다.")
    public ResponseEntity<ApiResponse<Void>> updateComment(@PathVariable long postId, @PathVariable long commentId, @RequestBody @Valid CommentRequest.UpdateDto req);

    @Operation(summary = "댓글 삭제", description = "본인의 댓글을 삭제합니다.")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable long postId, @PathVariable long commentId);
}
