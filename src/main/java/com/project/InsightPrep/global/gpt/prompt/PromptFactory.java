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

        String guardrails = switch (category.toLowerCase()) {
            case "algorithm" -> """
                    [카테고리: 알고리즘(코딩테스트/기술면접용)]
                    포함 예시: 시간복잡도/공간복잡도, 정렬, 탐색(Binary Search), 투포인터, 슬라이딩 윈도우, 스택/큐/우선순위큐/해시, 그래프(DFS/BFS), 최단경로(다익스트라/벨만-포드), 최소신장트리(크루스칼/프림), 
                    위상정렬, 동적계획법(LIS/LCS/Knapsack 등), 비트마스킹, 분할정복, 그리디, 유니온파인드, 세그먼트트리/펜윅트리 등.
                    반드시 제외: 머신러닝/딥러닝/통계/확률/최적화(경사하강법, CNN/RNN/Transformer, SVM, KMeans 등)
                    """;
            case "java" -> """
                    [카테고리: Java]
                    주제를 고르게 분산: 언어 기초(클래스/인터페이스/추상/상속/다형성), 제네릭/애너테이션/레코드, 예외/에러 처리, 컬렉션/동등성(equals/hashCode), 스트림/람다/함수형 인터페이스, 
                    동시성(스레드/락/volatile/Atomic/CompletableFuture), I/O/NIO, 모듈 시스템, JVM(클래스로더, 메모리 구조, JIT) 등. GC만 반복적으로 출제하지 말 것(필요 시 다른 주제와 교차).
                    프레임워크(Spring 등) 종속 질문은 피하고 순수 Java 중심으로.
                    """;
            case "os" -> """
                    [카테고리: 운영체제]
                    프로세스/스레드/CPU 스케줄링, 동기화(뮤텍스/세마포어/모니터), 교착상태,
                    메모리 관리(페이징/세그멘테이션/교체 알고리즘), 파일시스템, 시스템콜 등.
                    """;
            case "network", "computer network" -> """
                    [카테고리: 네트워크]
                    OSI/TCP-IP, TCP/UDP 차이/흐름·혼잡제어, 3-way/4-way, HTTP/1.1 vs 2 vs 3, TLS/HTTPS,
                    DNS/CDN/캐시, 프록시/로드밸런싱 등.
                    """;
            case "db", "database" -> """
                    [카테고리: 데이터베이스]
                    정규화/인덱스/트랜잭션/격리수준, 쿼리 최적화, 조인 전략, 샤딩/리플리케이션, NoSQL vs RDB 등.
                    """;
            case "spring" -> """
                    [카테고리: Spring]
                    DI/IoC, AOP, 트랜잭션 관리, MVC 구조, 빈 생명주기, 예외 처리, Validation 등(Java 일반보다 프레임워크 중심).
                    """;
            default -> """
                    [카테고리: 일반 CS]
                    자료구조/알고리즘/OS/네트워크/DB/소프트웨어 공학 등 면접용 정통 CS 범위에서 출제.
                    머신러닝/딥러닝/데이터사이언스 주제는 제외.
                    """;
        };

        String diversityRules = """
                추가 지침:
                - 비슷한 주제가 연속 반복되지 않도록, 최근에 출제된 주제와 중복을 피하세요.
                - 지나치게 광범위한 '모두 설명해라' 형태 대신, 한 개념/기법/상황을 날카롭게 파고드는 질문으로
                """;

        String userPrompt = """
            다음 카테고리에 대한 CS 면접 질문 1개를 생성하세요.
            카테고리: %s
    
            %s
    
            %s
        """.formatted(category, guardrails, diversityRules);

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
              "improvement": "...",            // 답변이 맞았는지 여부와 함께 더 좋은 답변을 위한 개선 방향이나 추가 설명을 제공합니다.
              "modelAnswer": "..."             // 면접관 입장에서 자신이라면 어떻게 대답을 할 것 같은지 질문에 대한 정답을 제공합니다.
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
