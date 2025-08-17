package com.project.InsightPrep.domain.post.exception;

import com.project.InsightPrep.global.common.response.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PostErrorCode implements BaseErrorCode {

    FORBIDDEN_OR_NOT_FOUND_ANSWER("FORBIDDEN_OR_NOT_FOUND_ANSWER", HttpStatus.BAD_REQUEST, "본인 답변이 아니거나 존재하지 않습니다."),
    CREATE_FAILED("CREATE_FAILED", HttpStatus.BAD_REQUEST, "게시글 생성에 실패했습니다.");

    private final String code;
    private final HttpStatus status;
    private final String message;
}
