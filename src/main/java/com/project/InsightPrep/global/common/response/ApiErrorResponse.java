package com.project.InsightPrep.global.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.project.InsightPrep.global.common.response.code.BaseErrorCode;

@JsonInclude(Include.NON_NULL)
public record ApiErrorResponse(String code, String message) {

    public static ApiErrorResponse of(BaseErrorCode errorCode) {
        return new ApiErrorResponse(errorCode.getCode(), errorCode.getMessage());
    }

    public static ApiErrorResponse of(String code, String message) {
        return new ApiErrorResponse(code, message);
    }
}
