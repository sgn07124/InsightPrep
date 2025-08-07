package com.project.InsightPrep.domain.question.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Builder;
import lombok.Getter;

public class AnswerResponse {

    @Getter
    @Builder
    @JsonInclude(Include.NON_NULL)
    public static class AnswerDto {

        private long id;
        private String content;
    }

    @Getter
    @Builder
    @JsonInclude(Include.NON_NULL)
    public static class FeedbackDto {

        private long feedbackId;
        private long questionId;
        private long answerId;

        private int score;
        private String improvement;  // 요약 및 정리
        private String modelAnswer;  // 개선점 제안
    }
}
