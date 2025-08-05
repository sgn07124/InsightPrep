package com.project.InsightPrep.global.gpt.dto.request;

import com.project.InsightPrep.global.gpt.dto.response.GptMessage;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GptRequest {
    private String model;
    private List<GptMessage> messages;
    private double temperature;
    private int max_tokens;
}
