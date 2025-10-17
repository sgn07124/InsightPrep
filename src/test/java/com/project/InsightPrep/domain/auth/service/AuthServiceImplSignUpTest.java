package com.project.InsightPrep.domain.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.project.InsightPrep.domain.auth.dto.request.AuthRequest.LoginDto;
import com.project.InsightPrep.domain.auth.dto.response.AuthResponse.LoginResultDto;
import com.project.InsightPrep.domain.auth.exception.AuthException;
import com.project.InsightPrep.domain.auth.repository.AuthRepository;
import com.project.InsightPrep.domain.member.entity.Member;
import com.project.InsightPrep.domain.member.entity.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplSignUpTest {

    @InjectMocks
    private AuthServiceImpl authService;

    @Mock
    private AuthRepository authRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    @BeforeEach
    void setUp() {
        // 세션 mock 설정
        ServletRequestAttributes attributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(attributes);

        Mockito.lenient().when(request.getSession(true)).thenReturn(session);
        Mockito.lenient().when(request.getSession(false)).thenReturn(session);
    }

    @Test
    @DisplayName("로그인 성공")
    void login_success() {
        // given
        LoginDto dto = new LoginDto("test@example.com", "Password123!", false);
        Member member = new Member(1L, "test@example.com", "Password123!","nickname", Role.USER, 10);

        when(authRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(dto.getPassword(), member.getPassword())).thenReturn(true);

        // when
        LoginResultDto result = authService.login(dto);

        // then
        assertEquals(member.getId(), result.getMemberId());
        assertEquals(member.getNickname(), result.getNickname());
        verify(session).setAttribute(eq(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY), any());
    }

    @Test
    @DisplayName("로그인 실패 - 이메일 없음")
    void login_email_not_exist() {
        // given
        LoginDto dto = new LoginDto("test@example.com", "Password123!", false);
        when(authRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());

        // expect
        assertThrows(AuthException.class, () -> authService.login(dto));
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void login_password_mismatch() {
        // given
        LoginDto dto = new LoginDto("test@example.com", "Password123", false);
        Member member = new Member(1L, "test@example.com", "Password123!","nickname", Role.USER, 10);

        when(authRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(dto.getPassword(), member.getPassword())).thenReturn(false);

        // expect
        assertThrows(AuthException.class, () -> authService.login(dto));
    }

    @Test
    @DisplayName("로그인 성공 - 자동로그인 세션 연장")
    void login_auto() {
        // given
        LoginDto dto = new LoginDto("test@example.com", "Password123!", true);
        Member member = new Member(1L, "test@example.com", "Password123!","nickname", Role.USER, 10);

        when(authRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(dto.getPassword(), member.getPassword())).thenReturn(true);

        // when
        authService.login(dto);

        // then
        verify(session).setMaxInactiveInterval(7 * 24 * 60 * 60);
    }
}
