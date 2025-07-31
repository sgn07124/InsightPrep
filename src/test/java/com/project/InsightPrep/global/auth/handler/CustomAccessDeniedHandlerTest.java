package com.project.InsightPrep.global.auth.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

class CustomAccessDeniedHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("AccessDeniedHandler - 403 상태코드와 메시지 반환 확인")
    void handle_shouldSetForbiddenStatusAndReturnJsonMessage() throws IOException {
        // given
        CustomAccessDeniedHandler handler = new CustomAccessDeniedHandler();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AccessDeniedException accessDeniedException = new AccessDeniedException("접근 거부");

        // when
        handler.handle(request, response, accessDeniedException);

        // then
        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentType()).isEqualTo("application/json;charset=UTF-8");

        String responseBody = response.getContentAsString();
        Map<String, Object> result = objectMapper.readValue(responseBody, Map.class);

        assertThat(result.get("code")).isEqualTo("FORBIDDEN");
        assertThat(result.get("message")).isEqualTo("접근 권한이 없습니다. 본인의 정보만 조회할 수 있습니다.");
    }
}