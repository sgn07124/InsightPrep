package com.project.InsightPrep.domain.question.event;

import com.project.InsightPrep.domain.question.dto.response.FeedbackResponse;
import com.project.InsightPrep.domain.question.entity.Answer;
import com.project.InsightPrep.domain.question.entity.AnswerFeedback;
import com.project.InsightPrep.domain.question.entity.Question;
import com.project.InsightPrep.domain.question.exception.QuestionErrorCode;
import com.project.InsightPrep.domain.question.exception.QuestionException;
import com.project.InsightPrep.domain.question.mapper.FeedbackMapper;
import com.project.InsightPrep.domain.question.repository.FeedbackRepository;
import com.project.InsightPrep.global.gpt.service.GptResponseType;
import com.project.InsightPrep.global.gpt.service.GptService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class FeedbackEventListenerTest {

    @Mock
    private GptService gptService;

    @Mock
    private FeedbackMapper feedbackMapper;

    @Mock
    private FeedbackRepository feedbackRepository;

    @InjectMocks
    private FeedbackEventListener feedbackEventListener;

    private Answer answer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        Question question = Question.builder()
                .id(1L)
                .content("OS에서 스레드와 프로세스의 차이점은?")
                .build();

        answer = Answer.builder()
                .id(10L)
                .question(question)
                .content("스레드는 프로세스 내 실행 단위입니다.")
                .build();
    }

    @Test
    @DisplayName("정상적인 AnswerSavedEvent가 주어지면 Feedback이 저장된다")
    void handleAnswerSaved_success() {
        // given
        FeedbackResponse mockResponse = FeedbackResponse.builder()
                .score(5)
                .modelAnswer("모델 답변")
                .improvement("개선 필요")
                .build();
        when(gptService.callOpenAI(anyList(), anyInt(), anyDouble(), eq(GptResponseType.FEEDBACK)))
                .thenReturn(mockResponse);

        AnswerSavedEvent event = new AnswerSavedEvent(answer);

        // when
        feedbackEventListener.handleAnswerSaved(event);

        // then
        ArgumentCaptor<AnswerFeedback> captor = ArgumentCaptor.forClass(AnswerFeedback.class);
        verify(feedbackRepository, times(1)).save(captor.capture());

        AnswerFeedback saved = captor.getValue();
        assertThat(saved.getAnswer()).isEqualTo(answer);
        assertThat(saved.getScore()).isEqualTo(5);
        assertThat(saved.getModelAnswer()).isEqualTo("모델 답변");
        assertThat(saved.getImprovement()).isEqualTo("개선 필요");
    }

    @Test
    @DisplayName("Answer가 null이면 예외가 발생한다")
    void handleAnswerSaved_nullAnswer() {
        // given
        AnswerSavedEvent event = new AnswerSavedEvent(null);

        // when & then
        assertThrows(QuestionException.class, () -> feedbackEventListener.handleAnswerSaved(event));
        verify(feedbackRepository, never()).save(any());
    }

    @Test
    @DisplayName("Answer에 Question 또는 content가 null이면 예외가 발생한다")
    void handleAnswerSaved_invalidAnswer() {
        Answer invalidAnswer = Answer.builder()
                .id(20L)
                .question(null) // 질문 없음
                .content(null) // 답변 없음
                .build();

        AnswerSavedEvent event = new AnswerSavedEvent(invalidAnswer);




        assertThrows(QuestionException.class, () -> feedbackEventListener.handleAnswerSaved(event));
        verify(feedbackRepository, never()).save(any());
    }
}