package com.project.InsightPrep.domain.question.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.project.InsightPrep.domain.member.entity.Member;
import com.project.InsightPrep.domain.question.dto.request.AnswerRequest.AnswerDto;
import com.project.InsightPrep.domain.question.entity.Answer;
import com.project.InsightPrep.domain.question.entity.AnswerStatus;
import com.project.InsightPrep.domain.question.entity.Question;
import com.project.InsightPrep.domain.question.mapper.AnswerMapper;
import com.project.InsightPrep.domain.question.mapper.QuestionMapper;
import com.project.InsightPrep.global.auth.util.SecurityUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AnswerServiceImplTest {

    @InjectMocks
    private AnswerServiceImpl answerService;

    @Mock
    private SecurityUtil securityUtil;

    @Mock
    private QuestionMapper questionMapper;

    @Mock
    private AnswerMapper answerMapper;

    @Mock
    private FeedbackServiceImpl feedbackService;

    @Test
    @DisplayName("답변 저장 - 정상 동작")
    void saveAnswer_success() {
        // given
        Long questionId = 1L;
        Member mockMember = Member.builder().id(1L).email("test@email.com").build();
        Question mockQuestion = Question.builder()
                .id(questionId)
                .content("질문 내용")
                .category("OS")
                .status(AnswerStatus.WAITING)
                .build();
        AnswerDto dto = new AnswerDto("테스트 답변입니다.");

        when(securityUtil.getAuthenticatedMember()).thenReturn(mockMember);
        when(questionMapper.findById(questionId)).thenReturn(mockQuestion);

        // doNothing은 void 메서드에 대해 설정
        doNothing().when(questionMapper).updateStatus(eq(questionId), anyString());
        doAnswer(invocation -> {
            Answer answer = invocation.getArgument(0);
            // answer.id 설정은 불필요 — 테스트 대상 아님
            return null;
        }).when(answerMapper).insertAnswer(any(Answer.class));
        doNothing().when(feedbackService).saveFeedback(any(Answer.class));

        // when
        answerService.saveAnswer(dto, questionId);

        // then
        verify(securityUtil).getAuthenticatedMember();
        verify(questionMapper).findById(questionId);
        verify(questionMapper).updateStatus(eq(questionId), eq("ANSWERED"));
        verify(answerMapper).insertAnswer(any(Answer.class));
        verify(feedbackService).saveFeedback(any(Answer.class));
    }

}