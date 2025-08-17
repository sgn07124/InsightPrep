package com.project.InsightPrep.domain.post.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class CommentResponse {

    @Getter
    @Builder
    public static class CommentRes {
        private long commentId;
        private String content;
        private long authorId;
        private String authorNickname;
        private long postId;
        private LocalDateTime createdAt;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentRow {
        private long id;
        private long postId;
        private long memberId;
        private String content;
    }
}
