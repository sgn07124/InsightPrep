package com.project.InsightPrep.global.gpt.dto.response;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GptResponse {

    private List<Choice> choices;

    @Getter
    @Setter
    public static class Choice {
        private GptMessage message;
    }
}
