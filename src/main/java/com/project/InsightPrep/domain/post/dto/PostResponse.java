package com.project.InsightPrep.domain.post.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class PostResponse {

    @Getter
    @AllArgsConstructor
    public static class Created {
        private Long postId;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PostDetailDto {
        private Long postId;
        private String title;
        private String content;
        private String status;          // OPEN / RESOLVED
        private LocalDateTime createdAt;

        private Long authorId;
        private String authorNickname;

        private Long questionId;
        private String category;
        private String question;

        private Long answerId;
        private String answer;

        private Long feedbackId;
        private Integer score;
        private String improvement;
        private String modelAnswer;
    }
}
