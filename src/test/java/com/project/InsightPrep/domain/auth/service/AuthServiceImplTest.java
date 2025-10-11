package com.project.InsightPrep.domain.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.project.InsightPrep.domain.auth.dto.request.AuthRequest.signupDto;
import com.project.InsightPrep.domain.auth.exception.AuthErrorCode;
import com.project.InsightPrep.domain.auth.exception.AuthException;
import com.project.InsightPrep.domain.auth.mapper.AuthMapper;
import com.project.InsightPrep.domain.auth.repository.AuthRepository;
import com.project.InsightPrep.domain.member.entity.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @InjectMocks
    private AuthServiceImpl authService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthMapper authMapper;

    @Mock
    private AuthRepository authRepository;

    @Mock
    private EmailService emailService;

    @Test
    @DisplayName("회원가입 성공 테스트")
    void signupSuccess() {
        // given
        signupDto dto = signupDto.builder()
                .email("test@example.com")
                .password("password123")
                .re_password("password123")
                .nickname("tester")
                .build();

        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encodedPassword");

        // when
        authService.signup(dto);

        // then
        verify(emailService).existEmail(dto.getEmail());
        verify(emailService).validateEmailVerified(dto.getEmail());
        verify(authRepository).save(any(Member.class));
    }

    @Test
    @DisplayName("비밀번호 불일치 테스트")
    void passwordUnMatch() {
        // given
        signupDto dto = signupDto.builder()
                .email("test@example.com")
                .password("password123")
                .re_password("wrongPassword")
                .nickname("tester")
                .build();

        doNothing().when(emailService).existEmail(dto.getEmail());
        doNothing().when(emailService).validateEmailVerified(dto.getEmail());

        // when & then
        assertThrows(AuthException.class, () -> authService.signup(dto));

        verify(authRepository, never()).save(any());
    }

    @Test
    @DisplayName("이메일 인증 안된 경우 테스트")
    void signup_이메일인증_안된경우_예외() {
        // given
        signupDto dto = signupDto.builder()
                .email("test@example.com")
                .password("password123")
                .re_password("password123")
                .nickname("tester")
                .build();

        // mock 설정: 이메일 중복은 아님
        doNothing().when(emailService).existEmail(dto.getEmail());
        // 이메일 인증 안됨 처리
        doThrow(new AuthException(AuthErrorCode.EMAIL_VERIFICATION_ERROR))
                .when(emailService).validateEmailVerified(dto.getEmail());

        // when & then
        assertThrows(AuthException.class, () -> authService.signup(dto));
        verify(authRepository, never()).save(any());
    }

}