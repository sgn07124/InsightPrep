package com.project.InsightPrep.domain.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.InsightPrep.domain.auth.dto.request.AuthRequest;
import com.project.InsightPrep.domain.auth.dto.response.AuthResponse.LoginResultDto;
import com.project.InsightPrep.domain.auth.exception.AuthErrorCode;
import com.project.InsightPrep.domain.auth.exception.AuthException;
import com.project.InsightPrep.domain.auth.service.AuthService;
import com.project.InsightPrep.domain.auth.service.EmailService;
import com.project.InsightPrep.global.auth.handler.CustomAccessDeniedHandler;
import com.project.InsightPrep.global.auth.handler.CustomAuthenticationEntryPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private EmailService emailService;

    @MockitoBean
    private CustomAccessDeniedHandler accessDeniedHandler;

    @MockitoBean
    private CustomAuthenticationEntryPoint authenticationEntryPoint;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("회원가입 성공 테스트")
    void signup_success() throws Exception {
        mockMvc.perform(post("/auth/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)  // 반드시 있어야 함
                        .content("""
                    {
                      "email": "test@example.com",
                      "password": "Pass123!!",
                      "re_password": "Pass123!!",
                      "nickname": "tester"
                    }
                """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SIGNUP_SUCCESS"));
    }

    // 이메일 전송 테스트
    @Test
    @DisplayName("이메일 전송 성공 테스트")
    void sendEmail_shouldReturnSuccess() throws Exception {
        String json = """
            {
              "email": "test@example.com"
            }
        """;

        mockMvc.perform(post("/auth/sendEmail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SEND_EMAIL_SUCCESS"));
    }

    @Test
    @DisplayName("이메일 인증 번호 인증 성공 테스트")
    void verifyEmail_success() throws Exception {
        // given
        String email = "test@example.com";
        String code = "ABC123";
        String json = """
            {
              "email": "%s",
              "code": "%s"
            }
        """.formatted(email, code);

        given(emailService.verifyCode(email, code)).willReturn(true);

        // when & then
        mockMvc.perform(post("/auth/verifyEmail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("VERIFIED_EMAIL_SUCCESS"));
    }

    @Test
    @DisplayName("이메일 인증 번호 인증 실패 테스트")
    void verifyEmail_fail_invalidCode() throws Exception {
        String email = "test@example.com";
        String code = "INVALID";

        String json = """
        {
          "email": "%s",
          "code": "%s"
        }
    """.formatted(email, code);

        given(emailService.verifyCode(email, code))
                .willThrow(new AuthException(AuthErrorCode.CODE_NOT_MATCH_ERROR));

        mockMvc.perform(post("/auth/verifyEmail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().is4xxClientError()); // 또는 isUnauthorized() 등
    }

    @Test
    @DisplayName("로그인 성공 테스트")
    void login_success() throws Exception {
        // given
        AuthRequest.LoginDto request = new AuthRequest.LoginDto("test@example.com", "Password123!", false);
        LoginResultDto resultDto = new LoginResultDto(1L, "테스트유저");

        given(authService.login(any())).willReturn(resultDto);

        // when & then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("LOGIN_SUCCESS"))
                .andExpect(jsonPath("$.result.nickname").value("테스트유저"));
    }

    @Test
    @DisplayName("로그인 유효성 검사 실패 테스트")
    void login_validation_fail() throws Exception {
        // given
        AuthRequest.LoginDto request = new AuthRequest.LoginDto("", "Password123!", false);

        // when & then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("EMAIL_NOTBLANK_ERROR")); // ApiErrorCode에 따라
    }

    @Test
    @DisplayName("로그인 인증 실패 예외 처리 테스트")
    void login_authentication_fail() throws Exception {
        // given
        AuthRequest.LoginDto request = new AuthRequest.LoginDto("test@example.com", "Password123!", false);

        given(authService.login(any()))
                .willThrow(new AuthenticationCredentialsNotFoundException("잘못된 인증"));

        // when & then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("NEED_LOGIN_ERROR"));
    }

    @Test
    @DisplayName("로그인 인가 실패 예외 처리 테스트")
    void login_Authorization_fail() throws Exception {
        // given
        AuthRequest.LoginDto request = new AuthRequest.LoginDto("test@example.com", "Password123!", false);

        given(authService.login(any()))
                .willThrow(new AuthorizationDeniedException("권한 없음"));

        // when & then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN_ERROR"));
    }
}