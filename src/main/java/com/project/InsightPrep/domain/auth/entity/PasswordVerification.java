package com.project.InsightPrep.domain.auth.entity;

import com.project.InsightPrep.domain.auth.exception.AuthErrorCode;
import com.project.InsightPrep.domain.auth.exception.AuthException;
import com.project.InsightPrep.global.auth.util.SecurityUtil;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "password_verification")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PasswordVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 254)
    private String email;

    @Column(name = "code_hash", nullable = false, length = 255)
    private String codeHash;

    // 남은 시도 횟수 (예: 기본 5회)
    @Column(nullable = false)
    private int attemptsLeft;

    // 1회용 처리 플래그
    @Column(nullable = false)
    private boolean used;

    // 만료 시각
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    // 감사/운영용
    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime usedAt;

    // 비밀번호 재설정 토큰(OTP 성공 후 발급)
    @Column(name = "reset_token", length = 255)
    private String resetToken;

    @Column(name = "reset_expires_at")
    private LocalDateTime resetExpiresAt;

    @Column(name = "reset_used", nullable = false)
    private boolean resetUsed;

    public PasswordVerification updateOtp(String codeHash, int attemptsLeft, boolean used, LocalDateTime expiresAt) {
        this.codeHash = codeHash;
        this.attemptsLeft = attemptsLeft;
        this.used = used;
        this.expiresAt = expiresAt;
        this.createdAt = LocalDateTime.now();
        return this;
    }

    public static PasswordVerification createNew(String email, String codeHash, int attemptsLeft, boolean used, LocalDateTime expiresAt) {
        return PasswordVerification.builder()
                .email(email)
                .codeHash(codeHash)
                .attemptsLeft(attemptsLeft)
                .used(used)
                .expiresAt(expiresAt)
                .createdAt(LocalDateTime.now())
                .resetUsed(false)
                .build();
    }

    /** OTP 유효성 검증 */
    public void validateOtp(SecurityUtil securityUtil, String inputCode) {
        if (this.used) {
            throw new AuthException(AuthErrorCode.OTP_ALREADY_USED);
        }

        if (this.expiresAt.isBefore(LocalDateTime.now())) {
            throw new AuthException(AuthErrorCode.EXPIRED_CODE_ERROR);
        }

        boolean matched = securityUtil.matches(inputCode, this.codeHash);
        if (!matched) {
            this.decreaseAttempts();
            if (this.attemptsLeft <= 0) {
                this.markOtpUsed();
                throw new AuthException(AuthErrorCode.OTP_INVALID);
            }
            throw new AuthException(AuthErrorCode.OTP_INVALID_ATTEMPT);
        }
    }

    /** OTP 시도 횟수를 감소시킵니다. */
    public void decreaseAttempts() {
        if (this.attemptsLeft > 0) {
            this.attemptsLeft -= 1;
        }
    }

    /** OTP를 사용 처리합니다. */
    public void markOtpUsed() {
        this.used = true;
        this.usedAt = LocalDateTime.now();
    }

    /** 새로운 비밀번호 재설정 토큰을 발급합니다. */
    public void issueResetToken(String token, LocalDateTime expiresAt) {
        this.resetToken = token;
        this.resetUsed = false;
        this.resetExpiresAt = expiresAt;
    }

    /** 재설정 토큰을 사용 처리합니다. */
    public void markResetTokenUsed() {
        this.resetUsed = true;
        this.resetToken = null;
        this.resetExpiresAt = null;
        this.usedAt = LocalDateTime.now();
    }
}
