package com.project.InsightPrep.domain.question.service.impl;

import com.project.InsightPrep.domain.member.entity.Member;
import com.project.InsightPrep.domain.question.dto.request.AnswerRequest.AnswerDto;
import com.project.InsightPrep.domain.question.entity.Answer;
import com.project.InsightPrep.domain.question.entity.Question;
import com.project.InsightPrep.domain.question.mapper.AnswerMapper;
import com.project.InsightPrep.domain.question.mapper.QuestionMapper;
import com.project.InsightPrep.domain.question.service.AnswerService;
import com.project.InsightPrep.global.auth.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AnswerServiceImpl implements AnswerService {

    private final SecurityUtil securityUtil;
    private final QuestionMapper questionMapper;
    private final AnswerMapper answerMapper;
    private final FeedbackServiceImpl feedbackService;

    @Override
    @Transactional
    public void saveAnswer(AnswerDto dto, Long questionId) {
        Member member = securityUtil.getAuthenticatedMember();
        Question question = questionMapper.findById(questionId);

        Answer answer = Answer.builder()
                .member(member)
                .question(question)
                .content(dto.getContent())
                .build();

        answerMapper.insertAnswer(answer);
        feedbackService.saveFeedback(answer);
    }
}
