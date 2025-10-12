package com.project.InsightPrep.domain.question.service.impl;

import com.project.InsightPrep.domain.member.entity.Member;
import com.project.InsightPrep.domain.question.dto.request.AnswerRequest.AnswerDto;
import com.project.InsightPrep.domain.question.dto.response.AnswerResponse;
import com.project.InsightPrep.domain.question.dto.response.PreviewResponse;
import com.project.InsightPrep.domain.question.entity.Answer;
import com.project.InsightPrep.domain.question.entity.Question;
import com.project.InsightPrep.domain.question.event.AnswerSavedEvent;
import com.project.InsightPrep.domain.question.exception.QuestionErrorCode;
import com.project.InsightPrep.domain.question.exception.QuestionException;
import com.project.InsightPrep.domain.question.mapper.AnswerMapper;
import com.project.InsightPrep.domain.question.mapper.QuestionMapper;
import com.project.InsightPrep.domain.question.repository.AnswerRepository;
import com.project.InsightPrep.domain.question.repository.QuestionRepository;
import com.project.InsightPrep.domain.question.service.AnswerService;
import com.project.InsightPrep.domain.question.service.FeedbackService;
import com.project.InsightPrep.global.auth.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AnswerServiceImpl implements AnswerService {

    private final SecurityUtil securityUtil;
    private final QuestionMapper questionMapper;
    private final QuestionRepository questionRepository;
    private final AnswerMapper answerMapper;
    private final AnswerRepository answerRepository;
    private final FeedbackService feedbackService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public AnswerResponse.AnswerDto saveAnswer(AnswerDto dto, Long questionId) {
        Member member = securityUtil.getAuthenticatedMember();
        Question question = questionRepository.findById(questionId).orElseThrow(() -> new QuestionException(QuestionErrorCode.QUESTION_NOT_FOUND));

        Answer answer = Answer.builder()
                .member(member)
                .question(question)
                .content(dto.getContent())
                .build();

        question.markAsAnswered();
        answerRepository.save(answer);
        //feedbackService.saveFeedback(answer);
        eventPublisher.publishEvent(new AnswerSavedEvent(answer));
        return AnswerResponse.AnswerDto.builder()
                .answerId(answer.getId()).build();
    }

    @Override
    @Transactional
    public void deleteAnswer(long answerId) {
        long memberId = securityUtil.getLoginMemberId();

        Answer answer = answerRepository.findById(answerId)
                .filter(a -> a.getMember().getId().equals(memberId))
                .orElseThrow(() -> new QuestionException(QuestionErrorCode.QUESTION_NOT_FOUND));

        Long questionId = answer.getQuestion().getId();

        try {
            answerRepository.delete(answer);
        } catch (EmptyResultDataAccessException e) {
            throw new QuestionException(QuestionErrorCode.ALREADY_DELETED);
        }

        if (answerRepository.countByQuestionId(questionId) == 0) {
            Question question = questionRepository.findById(questionId)
                    .orElseThrow(() -> new QuestionException(QuestionErrorCode.QUESTION_NOT_FOUND));
            question.markAsWaiting();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PreviewResponse getPreview(long answerId) {
        long memberId = securityUtil.getLoginMemberId();
        Answer answer = answerRepository.findByIdAndMemberId(answerId, memberId).orElseThrow(() -> new QuestionException(QuestionErrorCode.ANSWER_NOT_FOUND));
        Question question = answer.getQuestion();

        return PreviewResponse.builder()
                .question(question.getContent())
                .answer(answer.getContent())
                .build();
    }
}
