package com.project.InsightPrep.domain.question.service.impl;

import com.project.InsightPrep.domain.member.entity.Member;
import com.project.InsightPrep.domain.question.dto.request.AnswerRequest.AnswerDto;
import com.project.InsightPrep.domain.question.entity.Answer;
import com.project.InsightPrep.domain.question.entity.AnswerStatus;
import com.project.InsightPrep.domain.question.entity.Question;
import com.project.InsightPrep.domain.question.exception.QuestionErrorCode;
import com.project.InsightPrep.domain.question.exception.QuestionException;
import com.project.InsightPrep.domain.question.mapper.AnswerMapper;
import com.project.InsightPrep.domain.question.mapper.QuestionMapper;
import com.project.InsightPrep.domain.question.service.AnswerService;
import com.project.InsightPrep.domain.question.service.FeedbackService;
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
    private final FeedbackService feedbackService;

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

        questionMapper.updateStatus(questionId, AnswerStatus.ANSWERED.name());
        answerMapper.insertAnswer(answer);
        feedbackService.saveFeedback(answer);
    }

    @Override
    @Transactional
    public void deleteAnswer(long answerId) {
        long memberId = securityUtil.getLoginMemberId();

        Long questionId = answerMapper.findQuestionIdOfMyAnswer(answerId, memberId);
        if (questionId == null) {
            throw new QuestionException(QuestionErrorCode.QUESTION_NOT_FOUND);
        }

        int del = answerMapper.deleteMyAnswerById(answerId, memberId);
        if (del == 0) {
            throw new QuestionException(QuestionErrorCode.ALREADY_DELETED);
        }

        answerMapper.resetQuestionStatusIfNoAnswers(questionId, AnswerStatus.WAITING.name());
    }
}
