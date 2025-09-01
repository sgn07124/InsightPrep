package com.project.InsightPrep.domain.question.event;

import com.project.InsightPrep.domain.question.dto.response.FeedbackResponse;
import com.project.InsightPrep.domain.question.entity.Answer;
import com.project.InsightPrep.domain.question.entity.AnswerFeedback;
import com.project.InsightPrep.domain.question.exception.QuestionErrorCode;
import com.project.InsightPrep.domain.question.exception.QuestionException;
import com.project.InsightPrep.domain.question.mapper.FeedbackMapper;
import com.project.InsightPrep.global.gpt.prompt.PromptFactory;
import com.project.InsightPrep.global.gpt.service.GptResponseType;
import com.project.InsightPrep.global.gpt.service.GptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class FeedbackEventListener {

    private final GptService gptService;
    private final FeedbackMapper feedbackMapper;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAnswerSaved(AnswerSavedEvent event) {
        Answer answer = event.answer();
        if (answer == null) throw new QuestionException(QuestionErrorCode.ANSWER_NOT_FOUND);
        if (answer.getQuestion() == null) throw new QuestionException(QuestionErrorCode.QUESTION_NOT_FOUND);

        String question = answer.getQuestion().getContent();
        String userAnswer = answer.getContent();
        if (userAnswer == null) throw new QuestionException(QuestionErrorCode.ANSWER_NOT_FOUND);

        FeedbackResponse gptResult = gptService.callOpenAI(PromptFactory.forFeedbackGeneration(question, userAnswer), 1000, 0.4, GptResponseType.FEEDBACK);
        AnswerFeedback feedback = AnswerFeedback.builder()
                .answer(answer)
                .score(gptResult.getScore())
                .modelAnswer(gptResult.getModelAnswer())
                .improvement(gptResult.getImprovement())
                .build();

        feedbackMapper.insertFeedback(feedback);
        log.info("Feedback saved for Answer id = {}", answer.getId());
    }
}
