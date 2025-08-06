package com.project.InsightPrep.domain.question.service.impl;

import com.project.InsightPrep.domain.question.dto.response.FeedbackResponse;
import com.project.InsightPrep.domain.question.entity.Answer;
import com.project.InsightPrep.domain.question.entity.AnswerFeedback;
import com.project.InsightPrep.domain.question.mapper.FeedbackMapper;
import com.project.InsightPrep.domain.question.service.FeedbackService;
import com.project.InsightPrep.global.gpt.prompt.PromptFactory;
import com.project.InsightPrep.global.gpt.service.GptResponseType;
import com.project.InsightPrep.global.gpt.service.GptServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {

    private final GptServiceImpl gptService;
    private final FeedbackMapper feedbackMapper;

    @Transactional
    @Async
    public void saveFeedback(Answer answer) {
        String question = answer.getQuestion().getContent();
        String userAnswer = answer.getContent();

        FeedbackResponse gptResult = gptService.callOpenAI(PromptFactory.forFeedbackGeneration(question, userAnswer), 1000, 0.4, GptResponseType.FEEDBACK);
        System.out.println(gptResult.getScore());
        System.out.println(gptResult.getImprovement());
        System.out.println(gptResult.getModelAnswer());
        AnswerFeedback feedback = AnswerFeedback.builder()
                .answer(answer)
                .score(gptResult.getScore())
                .modelAnswer(gptResult.getModelAnswer())
                .improvement(gptResult.getImprovement())
                .build();

        feedbackMapper.insertFeedback(feedback);
    }
}
