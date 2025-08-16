package com.project.InsightPrep.domain.auth.exception;

import com.project.InsightPrep.global.common.response.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {

    // 유효성 검사
    PASSWORD_MATCH_ERROR("PASSWORD_MATCH_ERROR", HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
    LOGIN_FAIL("LOGIN_FAIL", HttpStatus.BAD_REQUEST, "이메일 또는 비밀번호가 일치하지 않습니다."),

    // 이메일
    EMAIL_DUPLICATE_ERROR("EMAIL_DUPLICATE_ERROR", HttpStatus.CONFLICT, "이미 존재하는 이메일입니다."),
    EMAIL_VERIFICATION_ERROR("EMAIL_VERIFICATION_ERROR", HttpStatus.BAD_REQUEST, "인증된 이메일이 아닙니다."),
    EXPIRED_CODE_ERROR("LINK_EXPIRED_ERROR", HttpStatus.BAD_REQUEST, "코드 입력 시간이 만료되었습니다."),
    ALREADY_SEND_CODE_ERROR("ALREADY_SEND_CODE_ERROR", HttpStatus.BAD_REQUEST, "이미 유효한 인증 코드가 발급되었습니다."),
    CODE_NOT_MATCH_ERROR("CODE_NOT_MATCH_ERROR", HttpStatus.BAD_REQUEST, "인증 코드가 일치하지 않습니다"),
    UNAUTHORIZED("UNAUTHORIZED", HttpStatus.UNAUTHORIZED, "비로그인 상태입니다."),
    MEMBER_NOT_FOUND("MEMBER_NOT_FOUND", HttpStatus.BAD_REQUEST, "회원을 찾을 수 없습니다."),
    NOT_AUTHENTICATED("NOT_AUTHENTICATED", HttpStatus.UNAUTHORIZED, "로그인 정보가 없습니다."),
    INVALID_AUTHENTICATION_PRINCIPAL("INVALID_AUTHENTICATION_PRINCIPAL", HttpStatus.FORBIDDEN, "인증 정보가 올바르지 않습니다."),
    NEED_LOGIN_ERROR("NEED_LOGIN_ERROR", HttpStatus.BAD_REQUEST, "로그인이 필요합니다.");


    private final String code;
    private final HttpStatus status;
    private final String message;
}
