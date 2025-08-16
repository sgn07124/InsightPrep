package com.project.InsightPrep.domain.auth.entity;

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
}
