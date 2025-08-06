package com.project.InsightPrep.domain.question.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(Include.NON_NULL)
public class FeedbackResponse {
    private int score;
    private String summary;
    private String improvement;
}
