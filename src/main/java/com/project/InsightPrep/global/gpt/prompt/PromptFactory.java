package com.project.InsightPrep.global.gpt.prompt;

import com.project.InsightPrep.global.gpt.dto.response.GptMessage;
import java.util.List;

public class PromptFactory {

    private PromptFactory() {} // static-only

    // 질문 생성용 프롬프트
    public static List<GptMessage> forQuestionGeneration(String category) {
        String systemPrompt = """
                당신은 예리하고 경험 많은 소프트웨어 개발자 면접관입니다. 
                지원자의 수준을 파악할 수 있는 깊이 있는 CS 면접 질문을 생성해야 합니다. 
                질문은 실무와 밀접하게 연관되며, 개념 이해를 바탕으로 응답자의 사고력을 평가할 수 있어야 합니다.
                응답은 질문 하나로만 구성되어야 하며, 질문 외의 설명이나 해설은 포함하지 마세요.
                아래 JSON 형식을 지켜서 응답해 주세요. 
                { \\"question\\": \\"...\\" }
                """;

        String userPrompt = category + "에 관한 CS(Computer System) 면접 질문 하나를 만들어 주세요.";
        return toMessages(systemPrompt, userPrompt);
    }

    // 공통 message 구성 로직
    private static List<GptMessage> toMessages(String systemPrompt, String userPrompt) {
        return List.of(
                new GptMessage("system", systemPrompt),
                new GptMessage("user", userPrompt)
        );
    }
}
