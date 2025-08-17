package com.project.InsightPrep.domain.auth.service;

import com.project.InsightPrep.domain.auth.entity.PasswordVerification;
import com.project.InsightPrep.domain.auth.exception.AuthException;
import com.project.InsightPrep.domain.auth.mapper.AuthMapper;
import com.project.InsightPrep.domain.auth.mapper.PasswordMapper;
import com.project.InsightPrep.domain.member.entity.Member;
import com.project.InsightPrep.global.auth.util.SecurityUtil;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceImplTest {

    @Mock EmailService emailService;
    @Mock PasswordMapper passwordMapper;
    @Mock AuthMapper authMapper;
    @Mock
    SecurityUtil securityUtil;

    @InjectMocks PasswordResetServiceImpl service;

    private static final String EMAIL = "user@example.com";

    @BeforeEach
    void setUp() {
        // no-op
    }

    @Test
    @DisplayName("requestOtp: 가입된 이메일이면 OTP 저장 및 메일 발송")
    void requestOtp_sendsMail_whenEmailExists() throws Exception {
        when(authMapper.existEmail(EMAIL)).thenReturn(true);

        // execute
        service.requestOtp(EMAIL);

        // verify: DB upsert & email sent
        verify(passwordMapper).upsertPasswordOtp(
                eq(EMAIL),
                anyString(),                       // codeHash (BCrypt)
                eq(5),                              // DEFAULT_ATTEMPTS
                eq(false),                          // used
                any(LocalDateTime.class),           // expiresAt
                any(LocalDateTime.class)            // createdAt
        );
        verify(emailService).sendEmail(eq(EMAIL), anyString(), contains("비밀번호 재설정 인증 코드"));
        verifyNoMoreInteractions(emailService, passwordMapper);
    }

    @Test
    @DisplayName("requestOtp: 미가입 이메일이면 아무 동작 없이 정상 종료 (정보 유출 방지)")
    void requestOtp_returnsSilently_whenEmailNotExist() throws Exception {
        when(authMapper.existEmail(EMAIL)).thenReturn(false);

        service.requestOtp(EMAIL);

        verifyNoInteractions(emailService);
        verifyNoInteractions(passwordMapper);
    }

    @Test
    @DisplayName("requestOtp: 메일 전송 실패 시 RuntimeException 전파")
    void requestOtp_throws_whenSendMailFails() throws Exception {
        when(authMapper.existEmail(EMAIL)).thenReturn(true);
        doThrow(new MessagingException("smtp down"))
                .when(emailService).sendEmail(anyString(), anyString(), anyString());

        assertThatThrownBy(() -> service.requestOtp(EMAIL))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("메일 전송 실패");
    }

    // ---------- verifyOtp ----------
    @Test
    @DisplayName("verifyOtp: 정상 - 코드 일치 시 사용 처리 후 resetToken 발급 및 저장")
    void verifyOtp_success_issueResetToken() {
        PasswordVerification row = otpRow(false, // used
                LocalDateTime.now().plusMinutes(5),
                "$2a$10$hash", // stored hash
                5,             // attempts
                false,         // resetUsed
                null,          // resetExpiresAt
                null           // resetToken
        );

        when(passwordMapper.findByEmail(EMAIL)).thenReturn(row);
        when(securityUtil.matches(eq("ABC123"), anyString())).thenReturn(true);
        when(passwordMapper.updateResetToken(eq(EMAIL), anyString(), eq(false), any(LocalDateTime.class)))
                .thenReturn(1);

        String token = service.verifyOtp(EMAIL, "ABC123");

        assertThat(token).isNotBlank();
        verify(passwordMapper).updateOtpAsUsed(EMAIL);
        verify(passwordMapper).updateResetToken(eq(EMAIL), anyString(), eq(false), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("verifyOtp: 코드 불일치(남은 시도 > 0) → attempts 감소 후 예외")
    void verifyOtp_decreaseAttempts_andThrow() {
        PasswordVerification row = otpRow(false, LocalDateTime.now().plusMinutes(5),
                "$2a$10$hash", 3, false, null, null);

        when(passwordMapper.findByEmail(EMAIL)).thenReturn(row);
        when(securityUtil.matches(eq("WRONG"), anyString())).thenReturn(false);

        assertThatThrownBy(() -> service.verifyOtp(EMAIL, "WRONG"))
                .isInstanceOf(AuthException.class);

        verify(passwordMapper).updateAttempts(EMAIL, 2);
        verify(passwordMapper, never()).updateOtpAsUsed(anyString());
    }

    @Test
    @DisplayName("verifyOtp: 코드 불일치(마지막 시도) → used 처리 후 OTP_INVALID")
    void verifyOtp_lastAttempt_marksUsed_andThrows() {
        PasswordVerification row = otpRow(false, LocalDateTime.now().plusMinutes(5),
                "$2a$10$hash", 1, false, null, null);

        when(passwordMapper.findByEmail(EMAIL)).thenReturn(row);
        when(securityUtil.matches(eq("WRONG"), anyString())).thenReturn(false);

        assertThatThrownBy(() -> service.verifyOtp(EMAIL, "WRONG"))
                .isInstanceOf(AuthException.class);

        verify(passwordMapper).updateOtpAsUsed(EMAIL);
        verify(passwordMapper, never()).updateAttempts(anyString(), anyInt());
    }

    @Test
    @DisplayName("verifyOtp: 이미 사용된 OTP")
    void verifyOtp_used_throws() {
        PasswordVerification row = otpRow(true, LocalDateTime.now().plusMinutes(5),
                "$2a$10$hash", 5, false, null, null);

        when(passwordMapper.findByEmail(EMAIL)).thenReturn(row);

        assertThatThrownBy(() -> service.verifyOtp(EMAIL, "ANY"))
                .isInstanceOf(AuthException.class);
    }

    @Test
    @DisplayName("verifyOtp: 만료된 OTP")
    void verifyOtp_expired_throws() {
        PasswordVerification row = otpRow(false, LocalDateTime.now().minusSeconds(1),
                "$2a$10$hash", 5, false, null, null);

        when(passwordMapper.findByEmail(EMAIL)).thenReturn(row);

        assertThatThrownBy(() -> service.verifyOtp(EMAIL, "ANY"))
                .isInstanceOf(AuthException.class);
    }

    // ---------- resetPassword ----------
    @Test
    @DisplayName("resetPassword: 정상 - 토큰 유효, 비번 업데이트 및 토큰 사용 처리")
    void resetPassword_success() {
        String token = UUID.randomUUID().toString();
        PasswordVerification row = otpRow(false, LocalDateTime.now().plusMinutes(5),
                null, 0, false, LocalDateTime.now().plusMinutes(10), token);
        row.builder().email(EMAIL).build();

        when(passwordMapper.findByResetToken(token)).thenReturn(row);
        when(authMapper.findByEmail(EMAIL)).thenReturn(Optional.of(Member.builder().email(EMAIL).build()));
        when(securityUtil.encode("newP@ss!")).thenReturn("hashed");
        when(authMapper.updatePasswordByEmail(EMAIL, "hashed")).thenReturn(1);
        when(passwordMapper.markResetTokenUsed(token)).thenReturn(1);

        service.resetPassword(token, "newP@ss!");

        verify(authMapper).updatePasswordByEmail(EMAIL, "hashed");
        verify(passwordMapper).markResetTokenUsed(token);
    }

    @Test
    @DisplayName("resetPassword: 토큰 조회 불가 → RESET_TOKEN_INVALID")
    void resetPassword_tokenNotFound() {
        when(passwordMapper.findByResetToken("bad")).thenReturn(null);

        assertThatThrownBy(() -> service.resetPassword("bad", "pw"))
                .isInstanceOf(AuthException.class);
    }

    @Test
    @DisplayName("resetPassword: 이미 사용된 토큰 → RESET_TOKEN_ALREADY_USED")
    void resetPassword_tokenAlreadyUsed() {
        PasswordVerification row = otpRow(false, LocalDateTime.now().plusMinutes(5),
                null, 0, true, LocalDateTime.now().plusMinutes(10), "token");
        when(passwordMapper.findByResetToken("token")).thenReturn(row);

        assertThatThrownBy(() -> service.resetPassword("token", "pw"))
                .isInstanceOf(AuthException.class);
    }

    @Test
    @DisplayName("resetPassword: 만료된 토큰 → RESET_TOKEN_EXPIRED")
    void resetPassword_tokenExpired() {
        PasswordVerification row = otpRow(false, LocalDateTime.now().plusMinutes(5),
                null, 0, false, LocalDateTime.now().minusSeconds(1), "token");
        when(passwordMapper.findByResetToken("token")).thenReturn(row);

        assertThatThrownBy(() -> service.resetPassword("token", "pw"))
                .isInstanceOf(AuthException.class);
    }

    @Test
    @DisplayName("resetPassword: 회원 없음 → MEMBER_NOT_FOUND")
    void resetPassword_memberNotFound() {
        String token = "token";
        PasswordVerification row = otpRow(false, LocalDateTime.now().plusMinutes(5),
                null, 0, false, LocalDateTime.now().plusMinutes(10), token);
        row.builder().email(EMAIL).build();

        when(passwordMapper.findByResetToken(token)).thenReturn(row);
        when(authMapper.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resetPassword(token, "pw"))
                .isInstanceOf(AuthException.class);
    }

    @Test
    @DisplayName("resetPassword: 비밀번호 업데이트 실패 → SERVER_ERROR")
    void resetPassword_updatePasswordFail() {
        String token = "token";
        PasswordVerification row = otpRow(false, LocalDateTime.now().plusMinutes(5),
                null, 0, false, LocalDateTime.now().plusMinutes(10), token);
        row.builder().email(EMAIL).build();

        when(passwordMapper.findByResetToken(token)).thenReturn(row);
        when(authMapper.findByEmail(EMAIL)).thenReturn(Optional.of(Member.builder().email(EMAIL).build()));
        when(securityUtil.encode("pw")).thenReturn("hashed");
        when(authMapper.updatePasswordByEmail(EMAIL, "hashed")).thenReturn(0);

        assertThatThrownBy(() -> service.resetPassword(token, "pw"))
                .isInstanceOf(AuthException.class);
    }

    @Test
    @DisplayName("resetPassword: 토큰 사용 처리 실패 → SERVER_ERROR")
    void resetPassword_markTokenFail() {
        String token = "token";
        PasswordVerification row = otpRow(false, LocalDateTime.now().plusMinutes(5),
                null, 0, false, LocalDateTime.now().plusMinutes(10), token);
        row.builder().email(EMAIL).build();

        when(passwordMapper.findByResetToken(token)).thenReturn(row);
        when(authMapper.findByEmail(EMAIL)).thenReturn(Optional.of(Member.builder().email(EMAIL).build()));
        when(securityUtil.encode("pw")).thenReturn("hashed");
        when(authMapper.updatePasswordByEmail(EMAIL, "hashed")).thenReturn(1);
        when(passwordMapper.markResetTokenUsed(token)).thenReturn(0);

        assertThatThrownBy(() -> service.resetPassword(token, "pw"))
                .isInstanceOf(AuthException.class);
    }

    private PasswordVerification otpRow(boolean used,
                                        LocalDateTime otpExpiresAt,
                                        String codeHash,
                                        int attemptsLeft,
                                        boolean resetUsed,
                                        LocalDateTime resetExpiresAt,
                                        String resetToken) {
        PasswordVerification p = PasswordVerification.builder()
                .email(EMAIL)
                .used(used)
                .expiresAt(otpExpiresAt)
                .codeHash(codeHash)
                .attemptsLeft(attemptsLeft)
                .resetUsed(resetUsed)
                .resetExpiresAt(resetExpiresAt)
                .resetToken(resetToken)
                .build();
        return p;
    }
}