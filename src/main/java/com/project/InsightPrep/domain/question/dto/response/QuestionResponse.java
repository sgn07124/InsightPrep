package com.project.InsightPrep.domain.question.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.project.InsightPrep.domain.question.entity.AnswerStatus;
import lombok.Builder;
import lombok.Getter;

public class QuestionResponse {

    @Getter
    @Builder
    @JsonInclude(Include.NON_NULL)
    public static class QuestionDto {

        private long id;
        private String content;
        private String category;
        private AnswerStatus status;
    }

    @Getter
    @Builder
    @JsonInclude(Include.NON_NULL)
    public static class GptQuestion {
        private String question;
        private String topic;
        private String keyword;
    }

    @Getter
    @Builder
    @JsonInclude(Include.NON_NULL)
    public static class QuestionsDto {
        private long questionId;
        private String category;
        private String question;

        private long answerId;
        private String answer;

        private long feedbackId;
        private int score;
        private String improvement;
        private String modelAnswer;
    }
}
