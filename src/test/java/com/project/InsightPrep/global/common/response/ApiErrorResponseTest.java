package com.project.InsightPrep.global.common.response;

import static org.assertj.core.api.Assertions.assertThat;

import com.project.InsightPrep.global.common.response.code.ApiErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ApiErrorResponseTest {

    @Test
    @DisplayName("ApiErrorCode 기반 에러 테스트")
    public void enumErrorTest() {
        //given
        ApiErrorCode errorCode = ApiErrorCode.BAD_REQUEST_ERROR;

        //when
        ApiErrorResponse response = ApiErrorResponse.of(errorCode);

        //then
        assertThat(response.code()).isEqualTo("BAD_REQUEST_ERROR");
        assertThat(response.message()).isEqualTo("잘못된 요청입니다.");
    }

    @Test
    @DisplayName("Custom 에러 테스트")
    public void customErrorTest() {
        //given
        String code = "CUSTOM_ERROR";
        String message = "커스텀 에러입니다.";

        //when
        ApiErrorResponse response = ApiErrorResponse.of(code, message);

        //then
        assertThat(response.code()).isEqualTo(code);
        assertThat(response.message()).isEqualTo(message);
    }
}