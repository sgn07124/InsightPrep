package com.project.InsightPrep.domain.question.service.impl;

import com.project.InsightPrep.domain.question.dto.response.PageResponse;
import com.project.InsightPrep.domain.question.dto.response.QuestionResponse;
import com.project.InsightPrep.domain.question.dto.response.QuestionResponse.GptQuestion;
import com.project.InsightPrep.domain.question.dto.response.QuestionResponse.QuestionDto;
import com.project.InsightPrep.domain.question.dto.response.QuestionResponse.QuestionsDto;
import com.project.InsightPrep.domain.question.entity.Answer;
import com.project.InsightPrep.domain.question.entity.AnswerFeedback;
import com.project.InsightPrep.domain.question.entity.AnswerStatus;
import com.project.InsightPrep.domain.question.entity.ItemType;
import com.project.InsightPrep.domain.question.entity.Question;
import com.project.InsightPrep.domain.question.mapper.AnswerMapper;
import com.project.InsightPrep.domain.question.mapper.QuestionMapper;
import com.project.InsightPrep.domain.question.repository.AnswerRepository;
import com.project.InsightPrep.domain.question.repository.QuestionRepository;
import com.project.InsightPrep.domain.question.service.QuestionService;
import com.project.InsightPrep.domain.question.service.RecentPromptFilterService;
import com.project.InsightPrep.global.auth.util.SecurityUtil;
import com.project.InsightPrep.global.gpt.dto.response.GptMessage;
import com.project.InsightPrep.global.gpt.prompt.PromptFactory;
import com.project.InsightPrep.global.gpt.service.GptResponseType;
import com.project.InsightPrep.global.gpt.service.GptService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final GptService gptService;
    private final QuestionMapper questionMapper;
    private final QuestionRepository questionRepository;
    private final AnswerMapper answerMapper;
    private final AnswerRepository answerRepository;
    private final RecentPromptFilterService recentPromptFilterService;
    private final SecurityUtil securityUtil;

    @Override
    @Transactional
    public QuestionDto createQuestion(String category) {
        long memberId = securityUtil.getLoginMemberId();
        // 1) 최근 금지 주제/키워드 조회 (없을 수 있음)
        List<String> bannedTopics = recentPromptFilterService.getRecent(memberId, category, ItemType.TOPIC, 10);
        List<String> bannedKeywords = recentPromptFilterService.getRecent(memberId, category, ItemType.KEYWORD, 10);

        // 2) 프롬프트 선택 (있으면 주입, 없으면 기본)
        List<GptMessage> prompt = (hasAny(bannedTopics, bannedKeywords))
                ? PromptFactory.forQuestionGeneration(category, bannedTopics, bannedKeywords)
                : PromptFactory.forQuestionGeneration(category);

        // 3) 호출
        GptQuestion gptQuestion = gptService.callOpenAI(prompt, 1000, 0.6, GptResponseType.QUESTION);

        // 4) DB에 저장
        Question question = Question.builder()
                .category(category)
                .content(gptQuestion.getQuestion())
                .status(AnswerStatus.WAITING)
                .build();

        questionRepository.save(question);

        // 5) 기록 (Redis + DB) - 응답에 topic/keyword가 비어있을 수도 있으므로 방어
        if (isNotBlank(gptQuestion.getTopic())) {
            recentPromptFilterService.record(memberId, category, ItemType.TOPIC, gptQuestion.getTopic());
        }
        if (isNotBlank(gptQuestion.getKeyword())) {
            recentPromptFilterService.record(memberId, category, ItemType.KEYWORD, gptQuestion.getKeyword());
        }

        return QuestionResponse.QuestionDto.builder()
                .id(question.getId())
                .content(question.getContent())
                .category(question.getCategory())
                .status(question.getStatus())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<QuestionsDto> getQuestions(int page, int size) {
        long memberId = securityUtil.getLoginMemberId();

        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 50);

        Pageable pageable = PageRequest.of(safePage - 1, safeSize);

        List<Answer> answers = answerRepository.findAllWithQuestionAndFeedbackByMemberId(memberId, pageable);

        List<QuestionsDto> dtos = answers.stream()
                .map(a -> {
                    AnswerFeedback f = a.getFeedback(); // fetch join으로 이미 로드됨
                    return QuestionResponse.QuestionsDto.builder()
                            .questionId(a.getQuestion().getId())
                            .category(a.getQuestion().getCategory())
                            .question(a.getQuestion().getContent())
                            .answerId(a.getId())
                            .answer(a.getContent())
                            .feedbackId(f.getId())
                            .score(f.getScore())
                            .improvement(f.getImprovement())
                            .modelAnswer(f.getModelAnswer())
                            .build();
                })
                .toList();

        return PageResponse.of(dtos, safePage, safeSize, dtos.size());
    }

    private boolean hasAny(List<String> a, List<String> b) {
        return (a != null && !a.isEmpty()) || (b != null && !b.isEmpty());
    }

    private boolean isNotBlank(String s) { return s != null && !s.isBlank(); }
}
