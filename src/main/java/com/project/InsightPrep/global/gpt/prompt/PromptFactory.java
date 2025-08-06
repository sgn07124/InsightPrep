package com.project.InsightPrep.global.gpt.prompt;

import com.project.InsightPrep.global.gpt.dto.response.GptMessage;
import java.util.List;

public class PromptFactory {

    private PromptFactory() {}

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

    public static List<GptMessage> forFeedbackGeneration(String question, String userAnswer) {
        String systemPrompt = """
            당신은 전문적이고 경험 많은 소프트웨어 개발 면접관입니다. 
            user의 질문에 대한 지원자의 답변을 읽고, 그 답변의 정확성과 완성도를 평가해야 합니다.

            평가 기준은 다음과 같습니다:
            1. 질문에 대한 개념적 이해와 설명이 적절한가?
            2. 실무적인 관점에서 충분한 설명이 이루어졌는가?
            3. 핵심 내용을 빠뜨리지 않았는가?
            
            출력은 아래 JSON 형식만 따릅니다. 다른 텍스트는 포함하지 마세요:
            {
              "score": ...,                // 0부터 100 사이의 점수. 점수는 정량적으로 명확하게 판단합니다.
              "summary": "...",            // 답변이 맞았는지 여부와 함께 정답 또는 보완 설명을 제공합니다.
              "improvement": "..."             // 더 좋은 답변을 위한 개선 방향이나 추가 설명이 있으면 작성합니다.
            }
            """;

        String userPrompt = String.format("""
            질문: %s
            사용자 답변: %s
            위 답변을 평가해 주세요.
            """, question, userAnswer);

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
