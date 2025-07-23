package com.project.InsightPrep.global.common.response.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ApiSuccessCode {

    SUCCESS("SUCCESS", HttpStatus.OK, "요청이 성공했습니다."),

    SIGNUP_SUCCESS("SIGNUP_SUCCESS", HttpStatus.OK, "회원가입이 완료되었습니다."),
    SEND_EMAIL_SUCCESS("SEND_EMAIL_SUCCESS", HttpStatus.OK, "이메일 전송 성공"),
    VERIFIED_EMAIL_SUCCESS("VERIFIED_EMAIL_SUCCESS", HttpStatus.OK, "이메일 인증 성공");

    private final String code;
    private final HttpStatus status;
    private final String message;
}
