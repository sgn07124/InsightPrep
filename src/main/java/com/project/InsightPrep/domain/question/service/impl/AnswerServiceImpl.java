package com.project.InsightPrep.domain.question.service.impl;

import com.project.InsightPrep.domain.member.entity.Member;
import com.project.InsightPrep.domain.question.dto.request.AnswerRequest.AnswerDto;
import com.project.InsightPrep.domain.question.dto.response.AnswerResponse;
import com.project.InsightPrep.domain.question.dto.response.PreviewResponse;
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
    public AnswerResponse.AnswerDto saveAnswer(AnswerDto dto, Long questionId) {
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
        return AnswerResponse.AnswerDto.builder()
                .answerId(answer.getId()).build();
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

    @Override
    @Transactional(readOnly = true)
    public PreviewResponse getPreview(long answerId) {
        long memberId = securityUtil.getLoginMemberId();
        PreviewResponse res = answerMapper.findMyPreviewByAnswerId(answerId, memberId);
        if (res == null) {
            throw new QuestionException(QuestionErrorCode.ANSWER_NOT_FOUND);
        }
        return res;
    }
}
