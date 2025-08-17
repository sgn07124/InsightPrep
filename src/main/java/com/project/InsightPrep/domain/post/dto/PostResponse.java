package com.project.InsightPrep.domain.post.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class PostResponse {

    @Getter
    @AllArgsConstructor
    public static class Created {
        private Long postId;
    }
}
