package com.project.InsightPrep.global.common.response;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.InsightPrep.global.common.response.code.ApiSuccessCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ApiResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("데이터 포함 응답 성공 테스트")
    public void resultTest() {
        //given
        String expectedCode = "SUCCESS";
        String expectedMessage = "요청이 성공했습니다.";
        String data = "테스트 데이터";

        //when
        ApiResponse<String> response = ApiResponse.of(ApiSuccessCode.SUCCESS, data);

        //then
        assertThat(response.code()).isEqualTo(expectedCode);
        assertThat(response.message()).isEqualTo(expectedMessage);
        assertThat(response.result()).isEqualTo(data);
    }

    @Test
    @DisplayName("데이터 미포함 응답 성공 테스트")
    public void resultNullTest() {
        //given
        String expectedCode = "SUCCESS";
        String expectedMessage = "요청이 성공했습니다.";

        //when
        ApiResponse<String> response = ApiResponse.of(ApiSuccessCode.SUCCESS);

        //then
        assertThat(response.code()).isEqualTo(expectedCode);
        assertThat(response.message()).isEqualTo(expectedMessage);
        assertThat(response.result()).isNull();
    }

    @Test
    @DisplayName("ApiResponse 직렬화 테스트 - result null")
    void testApiResponseWithNullResult() throws Exception {
        ApiResponse<String> response = ApiResponse.of(ApiSuccessCode.SUCCESS);

        String json = objectMapper.writeValueAsString(response);

        assertThat(json).contains("\"code\":\"SUCCESS\"");
        assertThat(json).contains("\"message\":\"요청이 성공했습니다.\"");
        assertThat(json).doesNotContain("result");
    }

    @Test
    @DisplayName("ApiResponse 직렬화 테스트 - result 값 존재")
    void testApiResponseWithResult() throws Exception {
        ApiResponse<String> response = ApiResponse.of(ApiSuccessCode.SUCCESS, "데이터 있음");

        String json = objectMapper.writeValueAsString(response);

        assertThat(json).contains("\"result\":\"데이터 있음\"");
    }
}