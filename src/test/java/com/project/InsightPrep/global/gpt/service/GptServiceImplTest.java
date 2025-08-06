package com.project.InsightPrep.global.gpt.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.InsightPrep.domain.question.dto.response.QuestionResponse;
import com.project.InsightPrep.global.gpt.dto.response.GptMessage;
import com.project.InsightPrep.global.gpt.dto.response.GptResponse;
import com.project.InsightPrep.global.gpt.exception.GptException;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class GptServiceImplTest {

    @Mock
    private WebClient gptWebClient;

    @Mock
    private WebClient.RequestBodyUriSpec uriSpec;

    @Mock
    private WebClient.RequestBodySpec bodySpec;

    @Mock
    private WebClient.RequestHeadersSpec<?> headersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private GptServiceImpl gptService;

    @Test
    @DisplayName("gpt 호출 테스트")
    void callOpenAI_success() throws Exception {
        // given
        List<GptMessage> prompts = List.of(new GptMessage("user", "질문 생성해줘"));
        GptResponseType responseType = GptResponseType.QUESTION;

        GptResponse mockResponse = new GptResponse();
        GptMessage message = new GptMessage("assistant", "{\"question\":\"운영체제란?\"}");
        GptResponse.Choice choice = new GptResponse.Choice();
        choice.setMessage(message);
        mockResponse.setChoices(List.of(choice));

        QuestionResponse.GptQuestion parsedQuestion = QuestionResponse.GptQuestion.builder()
                .question("운영체제란?")
                .build();

        // when - WebClient mock 체이닝
        when(gptWebClient.post()).thenReturn(uriSpec);
        when(uriSpec.uri("/chat/completions")).thenReturn(bodySpec);
        when(bodySpec.bodyValue(any())).thenAnswer(invocation -> headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(GptResponse.class)).thenReturn(Mono.just(mockResponse));


        // objectMapper 목
        when(objectMapper.readValue(Mockito.eq("{\"question\":\"운영체제란?\"}"),
                        Mockito.eq(QuestionResponse.GptQuestion.class)))
                .thenReturn(parsedQuestion);

        // when
        QuestionResponse.GptQuestion result = gptService.callOpenAI(prompts, 1000, 0.6, responseType);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertEquals("운영체제란?", result.getQuestion());
    }

    @Test
    @DisplayName("gpt 호출 테스트 - 예외 처리")
    void callOpenAI_gptResponseNull_throwsException() {
        // given
        Mockito.when(gptWebClient.post()).thenReturn(uriSpec);
        Mockito.when(uriSpec.uri("/chat/completions")).thenReturn(bodySpec);
        Mockito.when(bodySpec.bodyValue(Mockito.any())).thenAnswer(inv -> headersSpec);
        Mockito.when(headersSpec.retrieve()).thenReturn(responseSpec);
        Mockito.when(responseSpec.bodyToMono(GptResponse.class)).thenReturn(Mono.empty());

        // when & then
        assertThrows(GptException.class, () ->
                gptService.callOpenAI(List.of(new GptMessage("user", "text")), 1000, 0.6, GptResponseType.QUESTION)
        );
    }

    @Test
    @DisplayName("gpt 호출 테스트 - json 파싱 실패 시 예외 처리")
    void callOpenAI_jsonProcessingException_throwsException() throws Exception {
        // given
        String invalidJson = "invalid_json";

        GptMessage gptMessage = new GptMessage("assistant", invalidJson);
        GptResponse.Choice choice = new GptResponse.Choice();
        choice.setMessage(gptMessage);

        GptResponse mockGptResponse = new GptResponse();
        mockGptResponse.setChoices(List.of(choice));


        Mockito.when(gptWebClient.post()).thenReturn(uriSpec);
        Mockito.when(uriSpec.uri("/chat/completions")).thenReturn(bodySpec);
        Mockito.when(bodySpec.bodyValue(Mockito.any())).thenAnswer(invocation -> headersSpec);
        Mockito.when(headersSpec.retrieve()).thenReturn(responseSpec); // ✅ 꼭 필요함
        Mockito.when(responseSpec.bodyToMono(GptResponse.class)).thenReturn(Mono.just(mockGptResponse));

        // ObjectMapper가 JSON 파싱 실패하도록 예외 발생 설정
        Mockito.when(objectMapper.readValue(invalidJson, QuestionResponse.GptQuestion.class))
                .thenThrow(new JsonProcessingException("JSON 파싱 에러") {});

        // when & then
        assertThrows(GptException.class, () -> {
            gptService.callOpenAI(List.of(new GptMessage("user", "text")), 1000, 0.6, GptResponseType.QUESTION);
        });
    }
}