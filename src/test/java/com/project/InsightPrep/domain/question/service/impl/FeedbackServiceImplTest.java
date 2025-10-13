package com.project.InsightPrep.domain.question.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.project.InsightPrep.domain.question.dto.response.AnswerResponse.FeedbackDto;
import com.project.InsightPrep.domain.question.dto.response.FeedbackResponse;
import com.project.InsightPrep.domain.question.entity.Answer;
import com.project.InsightPrep.domain.question.entity.AnswerFeedback;
import com.project.InsightPrep.domain.question.entity.Question;
import com.project.InsightPrep.domain.question.exception.QuestionException;
import com.project.InsightPrep.domain.question.mapper.FeedbackMapper;
import com.project.InsightPrep.domain.question.repository.FeedbackRepository;
import com.project.InsightPrep.global.gpt.service.GptServiceImpl;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceImplTest {

    @InjectMocks
    private FeedbackServiceImpl feedbackService;

    @Mock
    private GptServiceImpl gptService;

    @Mock
    private FeedbackMapper feedbackMapper;

    @Mock
    private FeedbackRepository feedbackRepository;

    @Test
    @DisplayName("피드백 저장 - GPT 호출과 삽입 테스트")
    void saveFeedback_shouldCallGptAndInsertFeedback() {
        // given
        Question question = Question.builder()
                .id(1L)
                .content("멀티스레딩과 멀티프로세싱의 차이를 설명하라")
                .build();

        Answer answer = Answer.builder()
                .id(10L)
                .content("멀티스레딩은 자원 공유, 멀티프로세싱은 독립된 자원")
                .question(question)
                .build();

        FeedbackResponse gptResponse = FeedbackResponse.builder()
                .score(4)
                .improvement("예시를 추가하면 좋습니다.")
                .modelAnswer("멀티스레딩은 메모리를 공유하며...")
                .build();

        given(gptService.callOpenAI(any(), anyInt(), anyDouble(), any())).willReturn(gptResponse);

        // when
        feedbackService.saveFeedback(answer);

        // then
        ArgumentCaptor<AnswerFeedback> captor = ArgumentCaptor.forClass(AnswerFeedback.class);
        verify(feedbackRepository).save(captor.capture());

        AnswerFeedback savedFeedback = captor.getValue();
        assertEquals(4, savedFeedback.getScore());
        assertEquals("예시를 추가하면 좋습니다.", savedFeedback.getImprovement());
        assertEquals("멀티스레딩은 메모리를 공유하며...", savedFeedback.getModelAnswer());
        assertEquals(answer, savedFeedback.getAnswer());
    }

    @Test
    @DisplayName("피드백 조회 - 피드백 존재 시 조회 테스트")
    void getFeedback_shouldReturnDto_whenFeedbackExists() {
        // given
        Question question = Question.builder()
                .id(1L)
                .content("질문 내용")
                .build();

        Answer answer = Answer.builder()
                .id(2L)
                .content("답변 내용")
                .question(question)
                .build();

        AnswerFeedback feedback = AnswerFeedback.builder()
                .id(3L)
                .answer(answer)
                .score(5)
                .improvement("잘했지만 더 구체적인 설명 필요")
                .modelAnswer("정답 예시...")
                .build();

        given(feedbackRepository.findByAnswerId(2L)).willReturn(Optional.of(feedback));

        // when
        FeedbackDto result = feedbackService.getFeedback(2L);

        // then
        assertNotNull(result);
        assertEquals(3L, result.getFeedbackId());
        assertEquals(2L, result.getAnswerId());
        assertEquals(1L, result.getQuestionId());
        assertEquals(5, result.getScore());
        assertEquals("잘했지만 더 구체적인 설명 필요", result.getImprovement());
        assertEquals("정답 예시...", result.getModelAnswer());
    }

    @Test
    @DisplayName("피드백 조회 - 피드백 존재하지 않은 경우 null 리턴 테스트")
    void getFeedback_shouldReturnNull_whenFeedbackDoesNotExist() {
        // given
        Long answerId = 99L;
        given(feedbackRepository.findByAnswerId(99L)).willReturn(Optional.empty());

        // when
        var result = feedbackService.getFeedback(answerId);

        assertNull(result);
        then(feedbackRepository).should(times(1)).findByAnswerId(answerId);
    }
}