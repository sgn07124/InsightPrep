package com.project.InsightPrep.domain.post.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.InsightPrep.domain.post.dto.CommentRequest;
import com.project.InsightPrep.domain.post.dto.CommentResponse.CommentRes;
import com.project.InsightPrep.domain.post.service.CommentService;
import com.project.InsightPrep.domain.post.service.SharedPostService;
import com.project.InsightPrep.global.common.response.code.ApiSuccessCode;
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
                .andExpect(jsonPath("$.data.commentId").value(777L))
                .andExpect(jsonPath("$.data.content").value("첫 댓글"))
                .andExpect(jsonPath("$.data.postId").value((int) postId))
                .andExpect(jsonPath("$.code", anyOf(nullValue(), is(ApiSuccessCode.SUCCESS.name()))));

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
                .andExpect(jsonPath("$.code", anyOf(nullValue(), is(ApiSuccessCode.SUCCESS.name()))));

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
                .andExpect(jsonPath("$.code", anyOf(nullValue(), is(ApiSuccessCode.SUCCESS.name()))));

        verify(commentService).deleteComment(eq(postId), eq(commentId));
        verifyNoMoreInteractions(commentService);
    }
}