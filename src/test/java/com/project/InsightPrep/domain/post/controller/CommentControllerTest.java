package com.project.InsightPrep.domain.post.controller;

import static javax.swing.text.html.HTML.Tag.BASE;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.InsightPrep.domain.post.dto.CommentRequest;
import com.project.InsightPrep.domain.post.dto.CommentResponse.CommentListItem;
import com.project.InsightPrep.domain.post.dto.CommentResponse.CommentRes;
import com.project.InsightPrep.domain.post.exception.PostErrorCode;
import com.project.InsightPrep.domain.post.exception.PostException;
import com.project.InsightPrep.domain.post.service.CommentService;
import com.project.InsightPrep.domain.post.service.SharedPostService;
import com.project.InsightPrep.domain.question.dto.response.PageResponse;
import com.project.InsightPrep.global.common.response.code.ApiSuccessCode;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = PostController.class)
class CommentControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    CommentService commentService;

    @MockitoBean
    SharedPostService sharedPostService;

    @Test
    @DisplayName("POST /post/{postId}/comments - 성공")
    @WithMockUser(roles = "USER")
    void createComment_success() throws Exception {
        long postId = 10L;

        var req = new CommentRequest.CreateDto("첫 댓글");
        var res = CommentRes.builder()
                .commentId(777L)
                .content("첫 댓글")
                .authorId(1L)
                .authorNickname("tester")
                .postId(postId)
                .build();

        when(commentService.createComment(eq(postId), ArgumentMatchers.any(CommentRequest.CreateDto.class)))
                .thenReturn(res);

        mockMvc.perform(post("/post/{postId}/comments", postId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.commentId").value(777L))
                .andExpect(jsonPath("$.result.content").value("첫 댓글"))
                .andExpect(jsonPath("$.result.postId").value((int) postId))
                .andExpect(jsonPath("$.code", anyOf(nullValue(), is(ApiSuccessCode.CREATE_COMMENT_SUCCESS.name()))));

        verify(commentService).createComment(eq(postId), ArgumentMatchers.any(CommentRequest.CreateDto.class));
        verifyNoMoreInteractions(commentService);
    }

    @Test
    @DisplayName("PUT /post/{postId}/comments/{commentId} - 성공")
    @WithMockUser(roles = "USER")
    void updateComment_success() throws Exception {
        long postId = 10L;
        long commentId = 200L;

        var req = new CommentRequest.UpdateDto("수정된 내용");

        doNothing().when(commentService).updateComment(eq(postId), eq(commentId), ArgumentMatchers.any(CommentRequest.UpdateDto.class));

        mockMvc.perform(put("/post/{postId}/comments/{commentId}", postId, commentId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.code", anyOf(nullValue(), is(ApiSuccessCode.UPDATE_COMMENT_SUCCESS.name()))));

        verify(commentService).updateComment(eq(postId), eq(commentId), ArgumentMatchers.any(CommentRequest.UpdateDto.class));
        verifyNoMoreInteractions(commentService);
    }

    @Test
    @DisplayName("DELETE /post/{postId}/comments/{commentId} - 성공")
    @WithMockUser(roles = "USER")
    void deleteComment_success() throws Exception {
        long postId = 10L;
        long commentId = 200L;

        doNothing().when(commentService).deleteComment(eq(postId), eq(commentId));

        mockMvc.perform(delete("/post/{postId}/comments/{commentId}", postId, commentId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.code", anyOf(nullValue(), is(ApiSuccessCode.DELETE_COMMENT_SUCCESS.name()))));

        verify(commentService).deleteComment(eq(postId), eq(commentId));
        verifyNoMoreInteractions(commentService);
    }

    @Test
    @DisplayName("댓글 목록 조회 성공 - 기본 파라미터(page=1,size=10)와 응답 구조 검증")
    void listComments_success_defaultParams() throws Exception {
        long postId = 1L;
        int page = 1;
        int size = 10;

        List<CommentListItem> items = List.of(
                CommentListItem.builder()
                        .commentId(101L)
                        .authorId(11L)
                        .authorNickname("alice")
                        .content("first")
                        .createdAt(LocalDateTime.now().minusMinutes(2))
                        .mine(false)
                        .build(),
                CommentListItem.builder()
                        .commentId(102L)
                        .authorId(22L)
                        .authorNickname("bob")
                        .content("second")
                        .createdAt(LocalDateTime.now().minusMinutes(1))
                        .mine(true)
                        .build()
        );
        PageResponse<CommentListItem> pageRes = PageResponse.of(items, page, size, 2L);

        when(commentService.getComments(eq(postId), eq(page), eq(size)))
                .thenReturn(pageRes);

        mockMvc.perform(get("/post/{postId}/comments", postId)
                        .with(csrf())
                        .with(user("u1").roles("USER"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // ApiResponse 공통 래퍼 구조 검증
                .andExpect(jsonPath("$.code").value("GET_COMMENTS_SUCCESS"))
                .andExpect(jsonPath("$.message", not(isEmptyOrNullString())))
                // 페이징 필드
                .andExpect(jsonPath("$.result.page").value(page))
                .andExpect(jsonPath("$.result.size").value(size))
                .andExpect(jsonPath("$.result.totalElements").value(2))
                // 콘텐츠 일부 필드 검증
                .andExpect(jsonPath("$.result.content", hasSize(2)))
                .andExpect(jsonPath("$.result.content[0].commentId").value(101))
                .andExpect(jsonPath("$.result.content[0].authorNickname").value("alice"))
                .andExpect(jsonPath("$.result.content[0].mine").value(false))
                .andExpect(jsonPath("$.result.content[1].commentId").value(102))
                .andExpect(jsonPath("$.result.content[1].authorNickname").value("bob"))
                .andExpect(jsonPath("$.result.content[1].mine").value(true));
    }

    @Test
    @DisplayName("댓글 목록 조회 성공 - 커스텀 파라미터(page,size) 반영")
    void listComments_success_customParams() throws Exception {
        long postId = 2L;
        int page = 3;
        int size = 5;

        List<CommentListItem> items = List.of(); // 빈 페이지 예시
        PageResponse<CommentListItem> pageRes = PageResponse.of(items, page, size, 0L);

        when(commentService.getComments(eq(postId), eq(page), eq(size)))
                .thenReturn(pageRes);

        mockMvc.perform(get("/post/{postId}/comments", postId)
                        .with(csrf())
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
                        .with(user("u2").roles("USER"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("GET_COMMENTS_SUCCESS"))
                .andExpect(jsonPath("$.result.page").value(page))
                .andExpect(jsonPath("$.result.size").value(size))
                .andExpect(jsonPath("$.result.totalElements").value(0))
                .andExpect(jsonPath("$.result.content", hasSize(0)));
    }

    @Test
    @DisplayName("게시글이 없으면 POST_NOT_FOUND 에러 응답")
    void listComments_postNotFound() throws Exception {
        long postId = 404L;

        when(commentService.getComments(eq(postId), eq(1), eq(10)))
                .thenThrow(new PostException(PostErrorCode.POST_NOT_FOUND));

        mockMvc.perform(get("/post/{postId}/comments", postId)
                        .with(csrf())
                        .with(user("u3").roles("USER"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(PostErrorCode.POST_NOT_FOUND.name()))
                .andExpect(jsonPath("$.message", containsString(PostErrorCode.POST_NOT_FOUND.getMessage())));
    }
}