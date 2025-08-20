package com.project.InsightPrep.domain.post.exception;

import com.project.InsightPrep.global.common.response.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PostErrorCode implements BaseErrorCode {

    FORBIDDEN_OR_NOT_FOUND_ANSWER("FORBIDDEN_OR_NOT_FOUND_ANSWER", HttpStatus.BAD_REQUEST, "본인 답변이 아니거나 존재하지 않습니다."),
    CREATE_FAILED("CREATE_FAILED", HttpStatus.BAD_REQUEST, "게시글 생성에 실패했습니다."),
    POST_NOT_FOUND("POST_NOT_FOUND", HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."),
    FORBIDDEN("FORBIDDEN", HttpStatus.FORBIDDEN, "본인만 변경이 가능합니다."),
    ALREADY_RESOLVED("ALREADY_RESOLVED", HttpStatus.BAD_REQUEST, "이미 해결한 글입니다."),
    CONFLICT("CONFLICT", HttpStatus.CONFLICT, "수정에 실패했습니다."),

    COMMENT_NOT_FOUND("COMMENT_NOT_FOUND", HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다."),
    COMMENT_FORBIDDEN("COMMENT_FORBIDDEN", HttpStatus.FORBIDDEN, "댓글에 대한 권한이 없습니다.");

    private final String code;
    private final HttpStatus status;
    private final String message;
}
