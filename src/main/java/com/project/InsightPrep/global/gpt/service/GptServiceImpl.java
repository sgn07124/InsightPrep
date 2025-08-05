package com.project.InsightPrep.global.gpt.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.InsightPrep.domain.question.dto.response.QuestionResponse;
import com.project.InsightPrep.global.gpt.dto.request.GptRequest;
import com.project.InsightPrep.global.gpt.dto.response.CompanyAnalysisResponse;
import com.project.InsightPrep.global.gpt.dto.response.FeedbackResponse;
import com.project.InsightPrep.global.gpt.dto.response.GptMessage;
import com.project.InsightPrep.global.gpt.dto.response.GptResponse;
import com.project.InsightPrep.global.gpt.exception.GptErrorCode;
import com.project.InsightPrep.global.gpt.exception.GptException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class GptServiceImpl {

    private final WebClient gptWebClient;
    private final ObjectMapper objectMapper;

    @Value("${openai.model}")
    private String model;

    public <T> T callOpenAI(List<GptMessage> prompts, int maxTokens, double temperature, GptResponseType responseType) {
        GptRequest request = new GptRequest(
                model,
                prompts,
                temperature,
                maxTokens
        );

        GptResponse response = gptWebClient.post()
                .uri("/chat/completions")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(GptResponse.class)
                .block();

        if (response == null || response.getChoices().isEmpty()) {
            throw new GptException(GptErrorCode.GPT_RESPONSE_ERROR);
        }

        String content = response.getChoices().get(0).getMessage().getContent();

        try {
            return switch (responseType) {
                case FEEDBACK -> (T) objectMapper.readValue(content, FeedbackResponse.class);
                case QUESTION -> (T) objectMapper.readValue(content, QuestionResponse.GptQuestion.class);
                case ANALYSIS -> (T) objectMapper.readValue(content, CompanyAnalysisResponse.class);
            };
        } catch (JsonProcessingException e) {
            throw new GptException(GptErrorCode.GPT_PARSING_ERROR);
        }
    }
}
