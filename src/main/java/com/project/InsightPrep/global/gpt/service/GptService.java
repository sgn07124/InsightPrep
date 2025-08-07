package com.project.InsightPrep.global.gpt.service;

import com.project.InsightPrep.global.gpt.dto.response.GptMessage;
import java.util.List;

public interface GptService {

    <T> T callOpenAI(List<GptMessage> prompts, int maxTokens, double temperature, GptResponseType responseType);


}
