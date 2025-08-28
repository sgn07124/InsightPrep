package com.project.InsightPrep.global.gpt.prompt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.project.InsightPrep.global.gpt.dto.response.GptMessage;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class PromptFactoryTest {

    @Test
    @DisplayName("질문 생성 프롬프트 - 기본(금지목록 없음)")
    void forQuestionGeneration_basic_noBans() {
        // given
        String category = "운영체제";

        // when
        List<GptMessage> messages = PromptFactory.forQuestionGeneration(category);

        // then
        assertThat(messages).hasSize(2);

        GptMessage system = messages.get(0);
        GptMessage user = messages.get(1);

        assertThat(system.getRole()).isEqualTo("system");
        assertThat(system.getContent())
                .contains("예리하고 경험 많은 소프트웨어 개발자 면접관")
                .contains("JSON 형식을 지켜서 응답")
                .contains("JSON만 출력")
                .doesNotContain("```"); // 코드블록 금지 지시 포함 확인

        assertThat(user.getRole()).isEqualTo("user");
        assertThat(user.getContent())
                .contains("카테고리: " + category)
                .contains("출력 형식(JSON)")
                .doesNotContain("[중복 방지용 금지 목록]"); // 금지 목록 섹션 없음
    }

    @Test
    @DisplayName("질문 생성 프롬프트 - 금지 토픽/키워드 주입")
    void forQuestionGeneration_withBans() {
        // given
        String category = "Java";
        List<String> bannedTopics = List.of("volatile의 메모리 가시성", "GC 튜닝");
        List<String> bannedKeywords = List.of("volatile", "hashmap");

        // when
        List<GptMessage> messages = PromptFactory.forQuestionGeneration(category, bannedTopics, bannedKeywords);

        // then
        GptMessage user = messages.get(1);
        String content = user.getContent();

        assertThat(content).contains("[중복 방지용 금지 목록]");
        assertThat(content).contains("금지 토픽(최근): volatile의 메모리 가시성, GC 튜닝");
        assertThat(content).contains("금지 키워드(최근): volatile, hashmap");
        // Java 가드레일 존재
        assertThat(content).contains("[카테고리: Java]");
        // 특정 주제 반복 방지 지침 존재
        assertThat(content).contains("동일 카테고리 내에서도 하위 주제를 **순환**");
    }

    @Test
    @DisplayName("질문 생성 프롬프트 - 빈 금지목록 주입 시 기본 오버로드와 동일")
    void forQuestionGeneration_emptyBans_equalsNoBans() {
        // given
        String category = "OS";

        // when
        List<GptMessage> a = PromptFactory.forQuestionGeneration(category);
        List<GptMessage> b = PromptFactory.forQuestionGeneration(category, Collections.emptyList(), Collections.emptyList());

        // then
        assertThat(a.get(1).getContent()).isEqualTo(b.get(1).getContent());
        assertThat(a.get(0).getContent()).isEqualTo(b.get(0).getContent());
    }

    @ParameterizedTest(name = "질문 생성 프롬프트 - 카테고리 가드레일 삽입: {0}")
    @MethodSource("categoryGuardrails")
    void forQuestionGeneration_guardrails(GuardrailCase c) {
        // when
        List<GptMessage> messages = PromptFactory.forQuestionGeneration(c.input());

        // then
        GptMessage user = messages.get(1);
        assertThat(user.getContent()).contains(c.expectedMarker());
        assertThat(user.getContent()).contains("출력 형식(JSON):");
        assertThat(user.getContent()).contains("{ \"question\": \"...\", \"topic\": \"...\", \"keyword\": \"...\" }");
    }

    static Stream<GuardrailCase> categoryGuardrails() {
        return Stream.of(
                new GuardrailCase("algorithm", "[카테고리: 알고리즘(코딩테스트/기술면접용)]"),
                new GuardrailCase("java", "[카테고리: Java]"),
                new GuardrailCase("OS", "[카테고리: 운영체제]"),
                new GuardrailCase("network", "[카테고리: 네트워크]"),
                new GuardrailCase("computer network", "[카테고리: 네트워크]"),
                new GuardrailCase("db", "[카테고리: 데이터베이스]"),
                new GuardrailCase("database", "[카테고리: 데이터베이스]"),
                new GuardrailCase("spring", "[카테고리: Spring]"),
                new GuardrailCase("unknown-category", "[카테고리: 일반 CS]")
        );
    }

    @Test
    @DisplayName("시스템 프롬프트 - 질문/토픽/키워드 JSON 스키마 안내 포함")
    void systemPrompt_hasSchemaHints_forQuestion() {
        List<GptMessage> messages = PromptFactory.forQuestionGeneration("java");
        String system = messages.get(0).getContent();

        assertThat(system)
                .contains("JSON 형식을 지켜서 응답해 주세요")
                .contains("\\\"question\\\"")
                .contains("\\\"topic\\\"")
                .contains("\\\"keyword\\\"")
                .contains("JSON만 출력")
                .doesNotContain("```"); // 코드블록 금지
    }

    @Test
    @DisplayName("피드백 프롬프트 - system/user 메시지 형식 및 JSON 스키마 안내")
    void forFeedbackGeneration_basic() {
        // given
        String q = "HashMap과 ConcurrentHashMap의 차이는?";
        String a = "세그먼트 락으로 동시성 보장...";

        // when
        List<GptMessage> messages = PromptFactory.forFeedbackGeneration(q, a);

        // then
        assertThat(messages).hasSize(2);
        GptMessage system = messages.get(0);
        GptMessage user = messages.get(1);

        assertThat(system.getRole()).isEqualTo("system");
        assertThat(system.getContent())
                .contains("전문적이고 경험 많은 소프트웨어 개발 면접관")
                .contains("\"score\"")
                .contains("\"improvement\"")
                .contains("\"modelAnswer\"")
                .contains("JSON 형식만")
                .doesNotContain("```");

        assertThat(user.getRole()).isEqualTo("user");
        assertThat(user.getContent())
                .contains("질문: " + q)
                .contains("사용자 답변: " + a);
    }
    private record GuardrailCase(String input, String expectedMarker) {}
}