package com.project.InsightPrep.domain.question.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.project.InsightPrep.domain.member.entity.Member;
import com.project.InsightPrep.domain.question.dto.request.AnswerRequest.AnswerDto;
import com.project.InsightPrep.domain.question.dto.response.AnswerResponse;
import com.project.InsightPrep.domain.question.entity.Answer;
import com.project.InsightPrep.domain.question.entity.AnswerStatus;
import com.project.InsightPrep.domain.question.entity.Question;
import com.project.InsightPrep.domain.question.event.AnswerSavedEvent;
import com.project.InsightPrep.domain.question.exception.QuestionErrorCode;
import com.project.InsightPrep.domain.question.exception.QuestionException;
import com.project.InsightPrep.domain.question.repository.AnswerRepository;
import com.project.InsightPrep.domain.question.repository.QuestionRepository;
import com.project.InsightPrep.global.auth.util.SecurityUtil;
import java.lang.reflect.Field;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.EmptyResultDataAccessException;

@ExtendWith(MockitoExtension.class)
class AnswerServiceImplTest {

    @InjectMocks
    private AnswerServiceImpl answerService;

    @Mock
    private SecurityUtil securityUtil;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private AnswerRepository answerRepository;

    @Mock
    private FeedbackServiceImpl feedbackService;

    @Mock
    private ApplicationEventPublisher eventPublisher;



    @DisplayName("답변 저장 - 정상 동작")
    @Test
    void saveAnswer_success() {
        // given
        Long questionId = 1L;
        Member mockMember = Member.builder()
                .id(1L)
                .email("test@email.com")
                .build();

        Question mockQuestion = Question.builder()
                .id(questionId)
                .content("질문 내용")
                .category("OS")
                .status(AnswerStatus.WAITING)
                .build();

        AnswerDto dto = new AnswerDto("테스트 답변입니다.");

        when(securityUtil.getAuthenticatedMember()).thenReturn(mockMember);
        when(questionRepository.findById(questionId)).thenReturn(Optional.of(mockQuestion));

        // insertAnswer 호출 시, DB가 생성한 PK가 들어간 것처럼 id 세팅을 시뮬레이션
        doAnswer(invocation -> {
            Answer arg = invocation.getArgument(0);
            Field idField = Answer.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(arg, 100L);
            return null;
        }).when(answerRepository).save(any(Answer.class));

        // when
        AnswerResponse.AnswerDto res = answerService.saveAnswer(dto, questionId);

        // then
        verify(securityUtil).getAuthenticatedMember();
        verify(questionRepository).findById(questionId);
        verify(answerRepository).save(any(Answer.class));

        assertThat(mockQuestion.getStatus()).isEqualTo(AnswerStatus.ANSWERED);

        // 이벤트 객체 캡처
        ArgumentCaptor<AnswerSavedEvent> eventCaptor = ArgumentCaptor.forClass(AnswerSavedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        AnswerSavedEvent publishedEvent = eventCaptor.getValue();
        Answer savedForFeedback = publishedEvent.answer();  // 이벤트 안에서 Answer 꺼내기

        assertThat(savedForFeedback.getId()).isEqualTo(100L);
        assertThat(savedForFeedback.getMember().getId()).isEqualTo(1L);
        assertThat(savedForFeedback.getQuestion().getId()).isEqualTo(questionId);
        assertThat(savedForFeedback.getContent()).isEqualTo("테스트 답변입니다.");

        // 반환 DTO 검증 (서비스가 DTO를 반환하도록 구현되어 있다는 가정)
        assertThat(res).isNotNull();
        assertThat(res.getAnswerId()).isEqualTo(100L);
        verifyNoMoreInteractions(securityUtil, questionRepository, answerRepository, feedbackService);
    }

    @Test
    @DisplayName("실패: 내 답변이 아니거나 존재하지 않음 → QUESTION_NOT_FOUND")
    void deleteAnswer_questionNotFound() {
        // given
        long memberId = 1L;
        long answerId = 100L;

        when(securityUtil.getLoginMemberId()).thenReturn(memberId);
        when(answerRepository.findById(answerId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> answerService.deleteAnswer(answerId))
                .isInstanceOf(QuestionException.class)
                .matches(ex -> ((QuestionException) ex).getErrorCode() == QuestionErrorCode.QUESTION_NOT_FOUND);

        verify(securityUtil).getLoginMemberId();
        verify(answerRepository).findById(answerId);
        verify(answerRepository, never()).delete(any());
        verify(questionRepository, never()).findById(anyLong());
        verify(questionRepository, never()).save(any());
        verifyNoMoreInteractions(answerRepository, questionRepository, securityUtil);
    }

    @Test
    @DisplayName("실패: 이미 삭제된 답변 → ALREADY_DELETED (삭제 영향 행 0)")
    void deleteAnswer_alreadyDeleted() {
        // given
        long memberId = 1L;
        long answerId = 100L;
        long questionId = 10L;

        Member mockMember = Member.builder().id(memberId).build();
        Question mockQuestion = Question.builder().id(questionId).category("OS").content("Q").build();
        Answer mockAnswer = Answer.builder().id(answerId).member(mockMember).question(mockQuestion).content("A").build();

        when(securityUtil.getLoginMemberId()).thenReturn(memberId);
        when(answerRepository.findById(answerId)).thenReturn(Optional.of(mockAnswer));
        // 삭제 도중 예외 발생 확인 (이미 삭제된 상태 가정)
        doThrow(new EmptyResultDataAccessException(1)).when(answerRepository).delete(mockAnswer);

        // when & then
        assertThatThrownBy(() -> answerService.deleteAnswer(answerId))
                .isInstanceOf(QuestionException.class)
                .matches(ex -> ((QuestionException) ex).getErrorCode() == QuestionErrorCode.ALREADY_DELETED);

        verify(securityUtil).getLoginMemberId();
        verify(answerRepository).findById(answerId);
        verify(answerRepository).delete(mockAnswer);
        verifyNoMoreInteractions(answerRepository, questionRepository, securityUtil);
    }

    @Test
    void saveAnswer_shouldPublishEvent_andTriggerFeedbackListener() {
        // given
        Long questionId = 1L;
        AnswerDto dto = new AnswerDto("테스트 답변");

        Question mockQuestion = Question.builder()
                .id(questionId)
                .category("DB")
                .content("질문 내용")
                .status(AnswerStatus.WAITING)
                .build();

        when(questionRepository.findById(questionId)).thenReturn(Optional.of(mockQuestion));

        doAnswer(invocation -> {
            Answer arg = invocation.getArgument(0);
            Field idField = Answer.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(arg, 123L); // PK 강제 설정
            return arg;
        }).when(answerRepository).save(any(Answer.class));

        // when
        answerService.saveAnswer(dto, questionId);

        // then (비동기라 약간 대기 필요)
        Awaitility.await().atMost(3, TimeUnit.SECONDS).untilAsserted(() ->
                verify(eventPublisher, times(1)).publishEvent(any(AnswerSavedEvent.class))
        );

        assertThat(mockQuestion.getStatus()).isEqualTo(AnswerStatus.ANSWERED);
    }
}