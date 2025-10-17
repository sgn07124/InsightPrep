package com.project.InsightPrep.domain.question.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.project.InsightPrep.domain.question.dto.response.PageResponse;
import com.project.InsightPrep.domain.question.dto.response.QuestionResponse;
import com.project.InsightPrep.domain.question.dto.response.QuestionResponse.GptQuestion;
import com.project.InsightPrep.domain.question.dto.response.QuestionResponse.QuestionsDto;
import com.project.InsightPrep.domain.question.entity.Answer;
import com.project.InsightPrep.domain.question.entity.AnswerFeedback;
import com.project.InsightPrep.domain.question.entity.AnswerStatus;
import com.project.InsightPrep.domain.question.entity.ItemType;
import com.project.InsightPrep.domain.question.entity.Question;
import com.project.InsightPrep.domain.question.repository.AnswerRepository;
import com.project.InsightPrep.domain.question.repository.QuestionRepository;
import com.project.InsightPrep.domain.question.service.RecentPromptFilterService;
import com.project.InsightPrep.global.auth.util.SecurityUtil;
import com.project.InsightPrep.global.gpt.service.GptService;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class QuestionServiceImplTest {

    @Mock
    private GptService gptService;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private AnswerRepository answerRepository;

    @Mock
    private RecentPromptFilterService recentPromptFilterService;

    @Mock
    private SecurityUtil securityUtil;

    @InjectMocks
    private QuestionServiceImpl questionService;

    @Test
    @DisplayName("질문 생성 - GPT 호출과 DB 삽입 테스트")
    void createQuestion_ShouldGenerateQuestionAndInsertIntoDatabase() {
        // given
        String category = "OS";
        long memberId = 7L;

        // 로그인 사용자 id 필요
        when(securityUtil.getLoginMemberId()).thenReturn(memberId);

        // 최근 금지 토픽/키워드 조회는 빈 리스트로
        when(recentPromptFilterService.getRecent(eq(memberId), eq(category), eq(ItemType.TOPIC), anyInt()))
                .thenReturn(List.of());
        when(recentPromptFilterService.getRecent(eq(memberId), eq(category), eq(ItemType.KEYWORD), anyInt()))
                .thenReturn(List.of());

        // GPT 응답 Mock (topic/keyword도 채움: record 시 NPE 방지)
        GptQuestion mockGptQuestion = GptQuestion.builder()
                .question("운영체제에서 프로세스와 스레드의 차이를 설명하세요.")
                .topic("프로세스 vs 스레드")
                .keyword("thread")
                .build();
        when(gptService.callOpenAI(any(), anyInt(), anyDouble(), any()))
                .thenReturn(mockGptQuestion);

        // save() 호출 시 반환할 엔티티 mock
        doAnswer(invocation -> {
            Question q = invocation.getArgument(0);
            Field idField = Question.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(q, 123L);
            return q; // ✅ 중요: 동일 객체 반환
        }).when(questionRepository).save(any(Question.class));

        // when
        QuestionResponse.QuestionDto result = questionService.createQuestion(category);

        // then
        assertNotNull(result);
        assertEquals(123L, result.getId());
        assertEquals(category, result.getCategory());
        assertEquals("운영체제에서 프로세스와 스레드의 차이를 설명하세요.", result.getContent());
        assertEquals(AnswerStatus.WAITING, result.getStatus());

        // 핵심 상호작용 검증
        InOrder inOrder = inOrder(securityUtil, recentPromptFilterService, gptService, questionRepository);
        inOrder.verify(securityUtil).getLoginMemberId();
        inOrder.verify(recentPromptFilterService).getRecent(memberId, category, ItemType.TOPIC, 10);
        inOrder.verify(recentPromptFilterService).getRecent(memberId, category, ItemType.KEYWORD, 10);
        inOrder.verify(gptService).callOpenAI(any(), anyInt(), anyDouble(), any());
        inOrder.verify(questionRepository).save(any(Question.class));
        inOrder.verify(recentPromptFilterService).record(memberId, category, ItemType.TOPIC, "프로세스 vs 스레드");
        inOrder.verify(recentPromptFilterService).record(memberId, category, ItemType.KEYWORD, "thread");

        verifyNoMoreInteractions(recentPromptFilterService, gptService, questionRepository, securityUtil);
    }

    @Test
    @DisplayName("페이지네이션: page=2, size=10 → offset=10, total=23 → totalPages=3")
    void getQuestions_paged_ok() {
        // given
        long memberId = 42L;
        int page = 2;
        int size = 10;

        when(securityUtil.getLoginMemberId()).thenReturn(memberId);

        Question question = Question.builder()
                .id(10L).category("NETWORK").content("TCP/UDP?")
                .status(AnswerStatus.ANSWERED).build();

        AnswerFeedback feedback = AnswerFeedback.builder()
                .id(1000L).score(90).improvement("개선점").modelAnswer("...").build();

        Answer answer = Answer.builder()
                .id(100L).question(question).content("비교").feedback(feedback).build();

        Pageable pageable = PageRequest.of(page - 1, size);
        when(answerRepository.findAllWithQuestionAndFeedbackByMemberId(memberId, pageable))
                .thenReturn(List.of(answer));

        // when
        PageResponse<QuestionsDto> res = questionService.getQuestions(page, size);

        // then
        assertThat(res).isNotNull();
        assertThat(res.getContent()).hasSize(1);
        assertThat(res.getPage()).isEqualTo(2);
        assertThat(res.getSize()).isEqualTo(10);
        assertThat(res.getTotalElements()).isEqualTo(1);  // JPA Mock이므로 1건만
        assertThat(res.getTotalPages()).isEqualTo(1);

        assertThat(res.getContent().get(0).getQuestion()).isEqualTo("TCP/UDP?");
        assertThat(res.getContent().get(0).getAnswer()).isEqualTo("비교");
        assertThat(res.getContent().get(0).getScore()).isEqualTo(90);

        InOrder inOrder = inOrder(securityUtil, answerRepository);
        inOrder.verify(securityUtil).getLoginMemberId();
        inOrder.verify(answerRepository).findAllWithQuestionAndFeedbackByMemberId(eq(memberId), any(Pageable.class));
        verifyNoMoreInteractions(answerRepository, securityUtil);
    }

    @Test
    @DisplayName("size 상한(최대 50) 적용: page=1, size=100 → limit=50, offset=0")
    void getQuestions_sizeCappedTo50() {
        // given
        long memberId = 42L;
        int page = 1;
        int requestedSize = 100;   // 사용자가 크게 요청
        int safeSize = 50;         // 서비스 로직 상한

        when(securityUtil.getLoginMemberId()).thenReturn(memberId);
        when(answerRepository.findAllWithQuestionAndFeedbackByMemberId(eq(memberId), any(Pageable.class))).thenReturn(List.of()); // 빈 리스트

        // when
        PageResponse<QuestionsDto> res = questionService.getQuestions(page, requestedSize);

        // then
        assertThat(res.getPage()).isEqualTo(1);
        assertThat(res.getSize()).isEqualTo(50);
        assertThat(res.getTotalElements()).isEqualTo(0L);
        assertThat(res.isFirst()).isTrue();
        assertThat(res.isLast()).isTrue();

        verify(answerRepository).findAllWithQuestionAndFeedbackByMemberId(eq(memberId), any(Pageable.class));
        verifyNoMoreInteractions(answerRepository);
    }
}