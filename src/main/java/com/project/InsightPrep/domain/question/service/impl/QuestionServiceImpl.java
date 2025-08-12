package com.project.InsightPrep.domain.question.service.impl;

import com.project.InsightPrep.domain.member.entity.Member;
import com.project.InsightPrep.domain.question.dto.response.QuestionResponse;
import com.project.InsightPrep.domain.question.dto.response.QuestionResponse.GptQuestion;
import com.project.InsightPrep.domain.question.dto.response.QuestionResponse.QuestionDto;
import com.project.InsightPrep.domain.question.dto.response.QuestionResponse.QuestionsDto;
import com.project.InsightPrep.domain.question.entity.AnswerStatus;
import com.project.InsightPrep.domain.question.entity.Question;
import com.project.InsightPrep.domain.question.mapper.AnswerMapper;
import com.project.InsightPrep.domain.question.mapper.QuestionMapper;
import com.project.InsightPrep.domain.question.service.QuestionService;
import com.project.InsightPrep.global.auth.util.SecurityUtil;
import com.project.InsightPrep.global.gpt.prompt.PromptFactory;
import com.project.InsightPrep.global.gpt.service.GptResponseType;
import com.project.InsightPrep.global.gpt.service.GptService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final GptService gptService;
    private final QuestionMapper questionMapper;
    private final AnswerMapper answerMapper;
    private final SecurityUtil securityUtil;

    @Override
    @Transactional
    public QuestionDto createQuestion(String category) {
        GptQuestion gptQuestion = gptService.callOpenAI(PromptFactory.forQuestionGeneration(category), 1000, 0.6, GptResponseType.QUESTION);

        Question question = Question.builder()
                .category(category)
                .content(gptQuestion.getQuestion())
                .status(AnswerStatus.WAITING)
                .build();

        questionMapper.insertQuestion(question);

        return QuestionResponse.QuestionDto.builder()
                .id(question.getId())
                .content(question.getContent())
                .category(question.getCategory())
                .status(question.getStatus())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestionsDto> getQuestions() {
        long memberId = securityUtil.getLoginMemberId();
        return answerMapper.findQuestionsWithFeedback(memberId);
    }
}
