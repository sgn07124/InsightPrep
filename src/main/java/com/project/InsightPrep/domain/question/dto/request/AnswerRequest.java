package com.project.InsightPrep.domain.question.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class AnswerRequest {

    @Builder
    @Getter
    @Setter
    @JsonInclude(Include.NON_NULL)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerDto {

        @NotBlank(message = "답변을 작성해주세요.")
        private String content;
    }
}