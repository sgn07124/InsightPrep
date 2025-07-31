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
import org.springframework.security.core.AuthenticationException;

class CustomAuthenticationEntryPointTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("AuthenticationEntryPoint - 인증되지 않은 요청 401 응답 확인")
    void commence_shouldReturn401WithJsonBody() throws IOException {
        // given
        CustomAuthenticationEntryPoint entryPoint = new CustomAuthenticationEntryPoint();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AuthenticationException exception = new AuthenticationException("Unauthenticated") {};

        // when
        entryPoint.commence(request, response, exception);

        // then
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).isEqualTo("application/json;charset=UTF-8");

        String body = response.getContentAsString();
        Map<String, String> result = objectMapper.readValue(body, Map.class);

        assertThat(result.get("code")).isEqualTo("NEED_LOGIN_ERROR");
        assertThat(result.get("message")).isEqualTo("로그인이 필요한 요청입니다.");
    }
}