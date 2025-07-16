package com.project.InsightPrep.global.common.response.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ApiSuccessCode {

    SUCCESS("SUCCESS", HttpStatus.OK, "요청이 성공했습니다.");

    private final String code;
    private final HttpStatus status;
    private final String message;
}
