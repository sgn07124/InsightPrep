package com.project.InsightPrep.domain.auth.service;

import com.project.InsightPrep.domain.auth.entity.PasswordVerification;
import com.project.InsightPrep.domain.auth.exception.AuthErrorCode;
import com.project.InsightPrep.domain.auth.exception.AuthException;
import com.project.InsightPrep.domain.auth.repository.AuthRepository;
import com.project.InsightPrep.domain.auth.repository.PasswordRepository;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceImplTest {

    @Mock
    EmailService emailService;

    @Mock
    PasswordRepository passwordRepository;

    @Mock
    AuthRepository authRepository;

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
        when(authRepository.existsByEmail(EMAIL)).thenReturn(true);
        when(passwordRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        // execute
        service.requestOtp(EMAIL);

        // Repository save() 호출 검증
        verify(passwordRepository).save(argThat(saved -> {
            assertEquals(EMAIL, saved.getEmail());
            assertFalse(saved.isUsed());
            assertEquals(5, saved.getAttemptsLeft());
            assertNotNull(saved.getCodeHash());
            assertTrue(saved.getExpiresAt().isAfter(LocalDateTime.now()));
            return true;
        }));
        verify(emailService).sendEmail(eq(EMAIL), anyString(), contains("비밀번호 재설정 인증 코드"));
        verifyNoMoreInteractions(emailService, passwordRepository);
    }

    @Test
    @DisplayName("requestOtp: 미가입 이메일이면 아무 동작 없이 정상 종료 (정보 유출 방지)")
    void requestOtp_returnsSilently_whenEmailNotExist() throws Exception {
        when(authRepository.existsByEmail(EMAIL)).thenReturn(false);

        service.requestOtp(EMAIL);

        verifyNoInteractions(emailService);
        verifyNoInteractions(passwordRepository);
    }

    @Test
    @DisplayName("requestOtp: 메일 전송 실패 시 RuntimeException 전파")
    void requestOtp_throws_whenSendMailFails() throws Exception {
        when(authRepository.existsByEmail(EMAIL)).thenReturn(true);
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
        PasswordVerification otp = otpRow(false, // used
                LocalDateTime.now().plusMinutes(5),
                "$2a$10$hash", // stored hash
                5,             // attempts
                false,         // resetUsed
                null,          // resetExpiresAt
                null           // resetToken
        );

        when(passwordRepository.findByEmail(EMAIL)).thenReturn(Optional.of(otp));
        when(securityUtil.matches(eq("ABC123"), anyString())).thenReturn(true);

        String token = service.verifyOtp(EMAIL, "ABC123");

        assertThat(token).isNotBlank();
        assertThat(otp.isUsed()).isTrue();
        assertThat(otp.getResetToken()).isNotBlank();
        assertThat(otp.getResetExpiresAt()).isAfter(LocalDateTime.now());
    }

    @Test
    @DisplayName("verifyOtp: 코드 불일치(남은 시도 > 0) → attempts 감소 후 예외")
    void verifyOtp_decreaseAttempts_andThrow() {
        PasswordVerification otp = otpRow(false, LocalDateTime.now().plusMinutes(5),
                "$2a$10$hash", 3, false, null, null);

        when(passwordRepository.findByEmail(EMAIL)).thenReturn(Optional.of(otp));
        when(securityUtil.matches(eq("WRONG"), anyString())).thenReturn(false);

        assertThatThrownBy(() -> service.verifyOtp(EMAIL, "WRONG"))
                .isInstanceOf(AuthException.class);

        assertThat(otp.getAttemptsLeft()).isEqualTo(2);
        assertThat(otp.isUsed()).isFalse();
    }

    @Test
    @DisplayName("verifyOtp: 코드 불일치(마지막 시도) → used 처리 후 OTP_INVALID")
    void verifyOtp_lastAttempt_marksUsed_andThrows() {
        PasswordVerification otp = otpRow(false, LocalDateTime.now().plusMinutes(5),
                "$2a$10$hash", 1, false, null, null);

        when(passwordRepository.findByEmail(EMAIL)).thenReturn(Optional.of(otp));
        when(securityUtil.matches(eq("WRONG"), anyString())).thenReturn(false);

        assertThatThrownBy(() -> service.verifyOtp(EMAIL, "WRONG"))
                .isInstanceOf(AuthException.class)
                        .hasMessageContaining(AuthErrorCode.OTP_INVALID.getMessage());

        assertThat(otp.isUsed()).isTrue();
    }

    @Test
    @DisplayName("verifyOtp: 이미 사용된 OTP")
    void verifyOtp_used_throws() {
        PasswordVerification otp = otpRow(true, LocalDateTime.now().plusMinutes(5),
                "$2a$10$hash", 5, false, null, null);

        when(passwordRepository.findByEmail(EMAIL)).thenReturn(Optional.of(otp));

        assertThatThrownBy(() -> service.verifyOtp(EMAIL, "ANY"))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining(AuthErrorCode.OTP_ALREADY_USED.getMessage());
    }

    @Test
    @DisplayName("verifyOtp: 만료된 OTP")
    void verifyOtp_expired_throws() {
        PasswordVerification otp = otpRow(false, LocalDateTime.now().minusSeconds(1),
                "$2a$10$hash", 5, false, null, null);

        when(passwordRepository.findByEmail(EMAIL)).thenReturn(Optional.of(otp));

        assertThatThrownBy(() -> service.verifyOtp(EMAIL, "ANY"))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining(AuthErrorCode.EXPIRED_CODE_ERROR.getMessage());
    }

    // ---------- resetPassword ----------
    @Test
    @DisplayName("resetPassword: 정상 - 토큰 유효, 비번 업데이트 및 토큰 사용 처리")
    void resetPassword_success() {
        String token = UUID.randomUUID().toString();
        PasswordVerification otp = otpRow(false, LocalDateTime.now().plusMinutes(5),
                null, 0, false, LocalDateTime.now().plusMinutes(10), token);

        Member member = Member.builder().email(EMAIL).build();

        when(passwordRepository.findByResetToken(token)).thenReturn(Optional.of(otp));
        when(authRepository.findByEmail(EMAIL)).thenReturn(Optional.of(member));
        when(securityUtil.encode("newP@ss!")).thenReturn("hashed");

        service.resetPassword(token, "newP@ss!");

        assertThat(member.getPassword()).isEqualTo("hashed");
        assertThat(otp.isResetUsed()).isTrue();
        assertThat(otp.getResetToken()).isNull();
        assertThat(otp.getResetExpiresAt()).isNull();
    }

    @Test
    @DisplayName("resetPassword: 토큰 조회 불가 → RESET_TOKEN_INVALID")
    void resetPassword_tokenNotFound() {
        when(passwordRepository.findByResetToken("bad")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resetPassword("bad", "pw"))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining(AuthErrorCode.RESET_TOKEN_INVALID.getMessage());
    }

    @Test
    @DisplayName("resetPassword: 이미 사용된 토큰 → RESET_TOKEN_ALREADY_USED")
    void resetPassword_tokenAlreadyUsed() {
        PasswordVerification otp = otpRow(false, LocalDateTime.now().plusMinutes(5),
                null, 0, true, LocalDateTime.now().plusMinutes(10), "token");
        when(passwordRepository.findByResetToken("token")).thenReturn(Optional.of(otp));

        assertThatThrownBy(() -> service.resetPassword("token", "pw"))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining(AuthErrorCode.RESET_TOKEN_ALREADY_USED.getMessage());
    }

    @Test
    @DisplayName("resetPassword: 만료된 토큰 → RESET_TOKEN_EXPIRED")
    void resetPassword_tokenExpired() {
        PasswordVerification otp = otpRow(false, LocalDateTime.now().plusMinutes(5),
                null, 0, false, LocalDateTime.now().minusSeconds(1), "token");
        when(passwordRepository.findByResetToken("token")).thenReturn(Optional.of(otp));

        assertThatThrownBy(() -> service.resetPassword("token", "pw"))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining(AuthErrorCode.RESET_TOKEN_EXPIRED.getMessage());
    }

    @Test
    @DisplayName("resetPassword: 회원 없음 → MEMBER_NOT_FOUND")
    void resetPassword_memberNotFound() {
        String token = "token";
        PasswordVerification otp = otpRow(false, LocalDateTime.now().plusMinutes(5),
                null, 0, false, LocalDateTime.now().plusMinutes(10), token);

        when(passwordRepository.findByResetToken(token)).thenReturn(Optional.of(otp));
        when(authRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.resetPassword(token, "pw"))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining(AuthErrorCode.MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("resetPassword: 비밀번호 인코딩 실패 → SERVER_ERROR")
    void resetPassword_encodeFail() {
        String token = "token";
        PasswordVerification otp = otpRow(false, LocalDateTime.now().plusMinutes(5),
                null, 0, false, LocalDateTime.now().plusMinutes(10), token);

        Member member = Member.builder().email(EMAIL).build();

        when(passwordRepository.findByResetToken(token)).thenReturn(Optional.of(otp));
        when(authRepository.findByEmail(EMAIL)).thenReturn(Optional.of(member));
        when(securityUtil.encode("pw")).thenThrow(new RuntimeException(AuthErrorCode.SERVER_ERROR.getMessage()));

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