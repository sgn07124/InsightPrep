package com.project.InsightPrep.global.gpt.prompt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.project.InsightPrep.global.gpt.dto.response.GptMessage;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PromptFactoryTest {

    @Test
    @DisplayName("질문 생성 프롬프트 테스트")
    void testForQuestionGeneration_shouldReturnValidMessages() {
        // given
        String category = "운영체제";

        // when
        List<GptMessage> messages = PromptFactory.forQuestionGeneration(category);

        // then
        assertThat(messages).hasSize(2);

        GptMessage systemMessage = messages.get(0);
        GptMessage userMessage = messages.get(1);

        assertThat(systemMessage.getRole()).isEqualTo("system");
        assertThat(systemMessage.getContent()).contains("당신은 예리하고 경험 많은 소프트웨어 개발자 면접관입니다");

        assertThat(userMessage.getRole()).isEqualTo("user");
        assertThat(userMessage.getContent()).contains(category);
        assertThat(userMessage.getContent()).contains("면접 질문");
    }

}