package com.project.InsightPrep.global.gpt.exception;

import com.project.InsightPrep.global.common.response.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GptErrorCode implements BaseErrorCode {

    GPT_RESPONSE_ERROR("GPT_RESPONSE_ERROR", HttpStatus.BAD_REQUEST, "GPT 응답이 없습니다."),
    GPT_PARSING_ERROR("GPT_PARSING_ERROR", HttpStatus.BAD_REQUEST, "GPT 응답 파싱 중 오류가 발생했습니다.");

    private final String code;
    private final HttpStatus status;
    private final String message;
}
