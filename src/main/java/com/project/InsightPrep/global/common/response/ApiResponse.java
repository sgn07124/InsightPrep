package com.project.InsightPrep.global.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.project.InsightPrep.global.common.response.code.ApiSuccessCode;

@JsonInclude(Include.NON_NULL)
public record ApiResponse<T>(String code, String message, T result) {

    public static <T> ApiResponse<T> of(ApiSuccessCode code, T result) {
        return new ApiResponse<>(code.getCode(), code.getMessage(), result);
    }

    public static <T> ApiResponse<T> of(ApiSuccessCode code) {
        return new ApiResponse<>(code.getCode(), code.getMessage(), null);
    }
}
