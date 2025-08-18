package com.project.InsightPrep.domain.post.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.InsightPrep.domain.post.dto.PostRequest;
import com.project.InsightPrep.domain.post.dto.PostResponse.PostDetailDto;
import com.project.InsightPrep.domain.post.dto.PostResponse.PostListItemDto;
import com.project.InsightPrep.domain.post.service.CommentService;
import com.project.InsightPrep.domain.post.service.SharedPostService;
import com.project.InsightPrep.domain.question.dto.response.PageResponse;
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
class PostControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    SharedPostService sharedPostService;

    @MockitoBean
    CommentService commentService;

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/posts : 글 생성 성공")
    void createPost_success() throws Exception {
        PostRequest.Create req = PostRequest.Create.builder()
                .title("제목")
                .content("본문")
                .answerId(123L)
                .build();

        given(sharedPostService.createPost(ArgumentMatchers.any(PostRequest.Create.class)))
                .willReturn(777L);

        mockMvc.perform(post("/post")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("CREATE_POST_SUCCESS"))
                .andExpect(jsonPath("$.result.postId").value(777L));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/posts/{postId} : 단건 조회 성공")
    void getPost_success() throws Exception {
        long postId = 7L;
        PostDetailDto dto = PostDetailDto.builder()
                .postId(postId)
                .title("제목")
                .content("본문")
                .status("OPEN")
                .createdAt(LocalDateTime.now())
                .authorId(10L)
                .authorNickname("작성자")
                .questionId(1L)
                .category("CS")
                .question("질문")
                .answerId(2L)
                .answer("답변")
                .feedbackId(null)
                .score(null)
                .improvement(null)
                .modelAnswer(null)
                .myPost(true)
                .commentCount(3L)
                .build();

        given(sharedPostService.getPostDetail(postId)).willReturn(dto);

        mockMvc.perform(get("/post/{postId}", postId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("GET_POST_SUCCESS"))
                .andExpect(jsonPath("$.result.postId").value((int) postId))
                .andExpect(jsonPath("$.result.title").value("제목"))
                .andExpect(jsonPath("$.result.status").value("OPEN"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("PATCH /api/posts/{postId}/resolve : 해결 처리 성공")
    void resolve_success() throws Exception {
        long postId = 5L;
        // void 메서드이므로 별도 stubbing 불필요 (예외 없음 가정)

        mockMvc.perform(patch("/post/{postId}/resolve", postId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.result").doesNotExist());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/posts : 목록 + 페이징 성공")
    void list_success() throws Exception {
        int page = 2, size = 3;

        List<PostListItemDto> items = List.of(
                PostListItemDto.builder()
                        .postId(101L).title("T1").status("OPEN")
                        .createdAt(LocalDateTime.now())
                        .question("Q1").category("CS")
                        .build(),
                PostListItemDto.builder()
                        .postId(102L).title("T2").status("RESOLVED")
                        .createdAt(LocalDateTime.now())
                        .question("Q2").category("Algorithm")
                        .build()
        );

        PageResponse<PostListItemDto> pageRes = PageResponse.of(items, page, size, 20L);

        given(sharedPostService.getPosts(page, size)).willReturn(pageRes);

        mockMvc.perform(get("/post")
                        .with(csrf())
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.result.page").value(page))
                .andExpect(jsonPath("$.result.size").value(size))
                .andExpect(jsonPath("$.result.totalElements").value(20))
                .andExpect(jsonPath("$.result.content", hasSize(2)))
                .andExpect(jsonPath("$.result.content[0].title").value("T1"))
                .andExpect(jsonPath("$.result.content[1].status").value("RESOLVED"));
    }
}