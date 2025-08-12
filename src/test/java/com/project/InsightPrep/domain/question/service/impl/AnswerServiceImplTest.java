package com.project.InsightPrep.domain.question.service.impl;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.project.InsightPrep.domain.member.entity.Member;
import com.project.InsightPrep.domain.question.dto.request.AnswerRequest.AnswerDto;
import com.project.InsightPrep.domain.question.entity.Answer;
import com.project.InsightPrep.domain.question.entity.AnswerStatus;
import com.project.InsightPrep.domain.question.entity.Question;
import com.project.InsightPrep.domain.question.exception.QuestionErrorCode;
import com.project.InsightPrep.domain.question.exception.QuestionException;
import com.project.InsightPrep.domain.question.mapper.AnswerMapper;
import com.project.InsightPrep.domain.question.mapper.QuestionMapper;
import com.project.InsightPrep.global.auth.util.SecurityUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
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

    @Test
    @DisplayName("성공: 내 답변 1건 삭제 → 피드백은 CASCADE, 남은 답변 없으면 질문 상태 WAITING으로")
    void deleteAnswer_success() {
        // given
        long memberId = 1L;
        long answerId = 100L;
        long questionId = 10L;

        when(securityUtil.getLoginMemberId()).thenReturn(memberId);
        when(answerMapper.findQuestionIdOfMyAnswer(answerId, memberId)).thenReturn(questionId);
        when(answerMapper.deleteMyAnswerById(answerId, memberId)).thenReturn(1); // 1건 삭제
        doNothing().when(answerMapper).resetQuestionStatusIfNoAnswers(questionId, AnswerStatus.WAITING.name());

        // when & then
        assertThatCode(() -> answerService.deleteAnswer(answerId)).doesNotThrowAnyException();

        // verify: 순서 중요하면 InOrder로
        InOrder inOrder = inOrder(securityUtil, answerMapper);
        inOrder.verify(securityUtil).getLoginMemberId();
        inOrder.verify(answerMapper).findQuestionIdOfMyAnswer(answerId, memberId);
        inOrder.verify(answerMapper).deleteMyAnswerById(answerId, memberId);
        inOrder.verify(answerMapper).resetQuestionStatusIfNoAnswers(
                eq(questionId), eq(AnswerStatus.WAITING.name())
        );
        verifyNoMoreInteractions(answerMapper, securityUtil);
    }

    @Test
    @DisplayName("실패: 내 답변이 아니거나 존재하지 않음 → QUESTION_NOT_FOUND")
    void deleteAnswer_questionNotFound() {
        // given
        long memberId = 1L;
        long answerId = 100L;

        when(securityUtil.getLoginMemberId()).thenReturn(memberId);
        when(answerMapper.findQuestionIdOfMyAnswer(answerId, memberId)).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> answerService.deleteAnswer(answerId))
                .isInstanceOf(QuestionException.class)
                .matches(ex -> ((QuestionException) ex).getErrorCode() == QuestionErrorCode.QUESTION_NOT_FOUND);

        verify(securityUtil).getLoginMemberId();
        verify(answerMapper).findQuestionIdOfMyAnswer(answerId, memberId);
        verify(answerMapper, never()).deleteMyAnswerById(anyLong(), anyLong());
        verify(answerMapper, never()).resetQuestionStatusIfNoAnswers(anyLong(), anyString());
        verifyNoMoreInteractions(answerMapper, securityUtil);
    }

    @Test
    @DisplayName("실패: 이미 삭제된 답변 → ALREADY_DELETED (삭제 영향 행 0)")
    void deleteAnswer_alreadyDeleted() {
        // given
        long memberId = 1L;
        long answerId = 100L;
        long questionId = 10L;

        when(securityUtil.getLoginMemberId()).thenReturn(memberId);
        when(answerMapper.findQuestionIdOfMyAnswer(answerId, memberId)).thenReturn(questionId);
        when(answerMapper.deleteMyAnswerById(answerId, memberId)).thenReturn(0); // 영향 0 → 이미 삭제

        // when & then
        assertThatThrownBy(() -> answerService.deleteAnswer(answerId))
                .isInstanceOf(QuestionException.class)
                .matches(ex -> ((QuestionException) ex).getErrorCode() == QuestionErrorCode.ALREADY_DELETED);

        verify(securityUtil).getLoginMemberId();
        verify(answerMapper).findQuestionIdOfMyAnswer(answerId, memberId);
        verify(answerMapper).deleteMyAnswerById(answerId, memberId);
        verify(answerMapper, never()).resetQuestionStatusIfNoAnswers(anyLong(), anyString());
        verifyNoMoreInteractions(answerMapper, securityUtil);
    }
}