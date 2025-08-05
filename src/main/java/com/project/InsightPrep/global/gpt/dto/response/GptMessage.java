package com.project.InsightPrep.global.gpt.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GptMessage {
    private String role;
    private String content;
}
