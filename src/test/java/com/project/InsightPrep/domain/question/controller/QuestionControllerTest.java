package com.project.InsightPrep.domain.question.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.InsightPrep.domain.question.dto.request.AnswerRequest;
import com.project.InsightPrep.domain.question.dto.response.AnswerResponse.FeedbackDto;
import com.project.InsightPrep.domain.question.dto.response.QuestionResponse;
import com.project.InsightPrep.domain.question.entity.AnswerStatus;
import com.project.InsightPrep.domain.question.service.AnswerService;
import com.project.InsightPrep.domain.question.service.FeedbackService;
import com.project.InsightPrep.domain.question.service.QuestionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = QuestionController.class)
class QuestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QuestionService questionService;

    @MockitoBean
    private AnswerService answerService;

    @MockitoBean
    private FeedbackService feedbackService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "USER")  // Spring Security에서 제공하며, 가짜 로그인 유저로 인증해줌
    @DisplayName("질문 생성 성공")
    void createQuestion_success() throws Exception {
        QuestionResponse.QuestionDto responseDto = QuestionResponse.QuestionDto.builder()
                .id(1L)
                .category("OS")
                .content("운영체제에서 프로세스와 스레드의 차이는?")
                .status(AnswerStatus.WAITING)
                .build();

        given(questionService.createQuestion("OS")).willReturn(responseDto);

        mockMvc.perform(post("/question/OS").with(csrf()))  // CSRF 토큰 추가
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("CREATE_QUESTION_SUCCESS"))
                .andExpect(jsonPath("$.result.content").value("운영체제에서 프로세스와 스레드의 차이는?"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("답변 저장 성공")
    void saveAnswer_정상작동() throws Exception {
        AnswerRequest.AnswerDto request = new AnswerRequest.AnswerDto("스레드는 프로세스 내에서 실행되는 작업 단위입니다.");

        mockMvc.perform(post("/question/1/answer").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SAVE_ANSWER_SUCCESS"));

        verify(answerService).saveAnswer(any(), eq(1L));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("피드백 조회 성공 - 피드백이 존재하는 경우")
    void getFeedback_exist() throws Exception {
        FeedbackDto dto = FeedbackDto.builder()
                .feedbackId(1L)
                .questionId(1L)
                .answerId(1L)
                .score(3)
                .modelAnswer("모범 답안입니다.")
                .improvement("이런 부분을 개선해보세요.")
                .build();

        given(feedbackService.getFeedback(1L)).willReturn(dto);

        mockMvc.perform(post("/question/1/feedback").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("GET_FEEDBACK_SUCCESS"))
                .andExpect(jsonPath("$.result.score").value(3));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("피드백 조회 실패(PENDING) - 피드백이 존재하지 않는 경우")
    void getFeedback_not_exist() throws Exception {
        given(feedbackService.getFeedback(1L)).willReturn(null);

        mockMvc.perform(post("/question/1/feedback").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("FEEDBACK_PENDING"));
    }
}