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
    CODE_NOT_MATCH_ERROR("CODE_NOT_MATCH_ERROR", HttpStatus.BAD_REQUEST, "인증 코드가 일치하지 않습니다");


    private final String code;
    private final HttpStatus status;
    private final String message;
}
