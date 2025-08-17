package com.project.InsightPrep.domain.post.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

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
}
