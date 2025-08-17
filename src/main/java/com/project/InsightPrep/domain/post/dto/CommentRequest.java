package com.project.InsightPrep.domain.post.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class CommentRequest {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateDto {
        @NotBlank(message = "댓글 내용을 입력해주세요.")
        private String content;
    }
}
