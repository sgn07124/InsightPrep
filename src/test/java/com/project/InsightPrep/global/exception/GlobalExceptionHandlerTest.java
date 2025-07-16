package com.project.InsightPrep.global.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = DummyTestController.class)
@Import(GlobalExceptionHandler.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("CustomException 처리 테스트")
    void handleCustomException() throws Exception {
        mockMvc.perform(get("/test/custom"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST_ERROR"));
    }

    @Test
    @DisplayName("알 수 없는 예외 처리 테스트")
    void handleUnknownException() throws Exception {
        mockMvc.perform(get("/test/unknown"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR"));
    }

    @Test
    @DisplayName("@Valid 유효성 검증 실패 테스트")
    void handleValidationError() throws Exception {
        String json = "{\"email\":\"\", \"password\":\"1234abcd\"}";

        mockMvc.perform(post("/test/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("이메일을 입력해주세요"));
    }

    @Test
    @DisplayName("Enum 파싱 실패 테스트")
    void handleEnumParseError() throws Exception {
        mockMvc.perform(get("/test/enum?type=INVALID"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("TYPE_MISMATCH_ERROR"));
    }

    @Test
    @DisplayName("날짜 파싱 실패 테스트")
    void handleDateParseError() throws Exception {
        mockMvc.perform(get("/test/date?date=invalid-date"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("DATE_INVALID_ERROR"));
    }
}