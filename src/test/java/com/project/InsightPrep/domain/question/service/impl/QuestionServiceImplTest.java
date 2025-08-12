package com.project.InsightPrep.domain.question.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.project.InsightPrep.domain.question.dto.response.QuestionResponse;
import com.project.InsightPrep.domain.question.dto.response.QuestionResponse.GptQuestion;
import com.project.InsightPrep.domain.question.dto.response.QuestionResponse.QuestionsDto;
import com.project.InsightPrep.domain.question.entity.AnswerStatus;
import com.project.InsightPrep.domain.question.entity.Question;
import com.project.InsightPrep.domain.question.mapper.AnswerMapper;
import com.project.InsightPrep.domain.question.mapper.QuestionMapper;
import com.project.InsightPrep.global.auth.util.SecurityUtil;
import com.project.InsightPrep.global.gpt.service.GptService;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class QuestionServiceImplTest {

    @Mock
    private GptService gptService;

    @Mock
    private QuestionMapper questionMapper;

    @Mock
    private AnswerMapper answerMapper;

    @Mock
    private SecurityUtil securityUtil;

    @InjectMocks
    private QuestionServiceImpl questionService;

    @Test
    @DisplayName("질문 생성 - GPT 호출과 DB 삽입 테스트")
    void createQuestion_ShouldGenerateQuestionAndInsertIntoDatabase() {
        // given
        String category = "OS";

        // GPT 응답 Mock 설정
        GptQuestion mockGptQuestion = GptQuestion.builder()
                        .question("운영체제에서 프로세스와 스레드의 차이를 설명하세요.").build();
        when(gptService.callOpenAI(any(), anyInt(), anyDouble(), any()))
                .thenReturn(mockGptQuestion);

        // Question 객체가 저장될 때, id가 설정되어 있다고 가정
        // MyBatis insert 후에 객체에 id가 설정되는 구조이므로, 직접 설정 필요
        // Entity에 @Setter를 두는 것을 선호하지 않기 때문에 리플렉션을 통해 id 필드에 강제로 값을 주입 (테스트 코드에서만 사용하므로 이 방식 적용)
        doAnswer(invocation -> {
            Question q = invocation.getArgument(0);

            Field idField = Question.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(q, 123L);  // id 직접 설정

            return null;
        }).when(questionMapper).insertQuestion(any(Question.class));

        // when
        QuestionResponse.QuestionDto result = questionService.createQuestion(category);

        // then
        assertNotNull(result);
        assertEquals(123L, result.getId());
        assertEquals(category, result.getCategory());
        assertEquals("운영체제에서 프로세스와 스레드의 차이를 설명하세요.", result.getContent());
        assertEquals(AnswerStatus.WAITING, result.getStatus());

        // insert가 실제로 호출되었는지 확인
        verify(questionMapper, times(1)).insertQuestion(any(Question.class));
    }

    @Test
    @DisplayName("로그인 한 사용자의 답변 및 피드백, 질문 목록 조회")
    void getQuestions() {
        // given: 인증 로직 무시 → SecurityUtil만 스텁
        long memberId = 42L;
        when(securityUtil.getLoginMemberId()).thenReturn(memberId);

        var dto = QuestionsDto.builder()
                .questionId(10L).category("NETWORK").question("TCP/UDP?")
                .answerId(100L).answer("비교")
                .feedbackId(1000L).score(90).modelAnswer("...").build();

        when(answerMapper.findQuestionsWithFeedback(memberId))
                .thenReturn(List.of(dto));

        // when
        var result = questionService.getQuestions();

        // then
        assertThat(result).hasSize(1);
        verify(securityUtil).getLoginMemberId();                 // 인증 부분은 호출만 확인
        verify(answerMapper).findQuestionsWithFeedback(42L);     // 핵심 상호작용 검증
        verifyNoMoreInteractions(answerMapper, securityUtil);
    }
}