package com.project.InsightPrep.domain.auth.service;

import com.project.InsightPrep.domain.auth.entity.PasswordVerification;
import com.project.InsightPrep.domain.auth.exception.AuthErrorCode;
import com.project.InsightPrep.domain.auth.exception.AuthException;
import com.project.InsightPrep.domain.auth.mapper.AuthMapper;
import com.project.InsightPrep.domain.auth.mapper.PasswordMapper;
import com.project.InsightPrep.domain.auth.repository.AuthRepository;
import com.project.InsightPrep.domain.member.entity.Member;
import com.project.InsightPrep.global.auth.util.SecurityUtil;
import jakarta.mail.MessagingException;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private static final long EXPIRE_MINUTES = 10L; // 10분
    private static final int DEFAULT_ATTEMPTS = 5;

    private final EmailService emailService;
    private final PasswordMapper passwordMapper;
    private final AuthMapper authMapper;
    private final AuthRepository authRepository;
    private final SecurityUtil securityUtil;

    @Override
    @Transactional
    public void requestOtp(String email) {
        boolean exists = authRepository.existsByEmail(email);
        // exists=false여도 "요청 접수" 응답은 동일하게. (계정 존재 여부 노출 방지)
        if (!exists) {
            // 계정이 없으면 그냥 "요청 접수"처럼 리턴 (메일 안보냄)
            return;
        }

        String code = generateCode(6);
        String codeHash = BCrypt.hashpw(code, BCrypt.gensalt());
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(EXPIRE_MINUTES);

        passwordMapper.upsertPasswordOtp(email, codeHash, DEFAULT_ATTEMPTS, false, expiresAt, now);

        String title = "InsightPrep 비밀번호 재설정 인증 코드";
        String content = """
                <html>
                  <body>
                    <h1>비밀번호 재설정 인증 코드</h1>
                    <p style="font-size:18px;"><b>%s</b></p>
                    <p>해당 코드를 10분 내에 입력해 주세요.</p>
                    <hr/>
                    <p style="color: grey; font-size: small;">
                      ※본 메일은 자동발송 메일입니다. 회신하지 마세요.
                    </p>
                  </body>
                </html>
                """.formatted(code);

        try {
            emailService.sendEmail(email, title, content);
        } catch (MessagingException | RuntimeException e) {
            throw new RuntimeException("메일 전송 실패", e);
        }
    }

    @Override
    @Transactional
    public String verifyOtp(String email, String inputCode) {
        // 1. 저장된 OTP 가져오기
        PasswordVerification otp = passwordMapper.findByEmail(email);
        if (otp == null) {
            throw new AuthException(AuthErrorCode.CODE_NOT_MATCH_ERROR);
        }

        // 2. 이미 사용 여부
        if (otp.isUsed()) {
            throw new AuthException(AuthErrorCode.OTP_ALREADY_USED);
        }

        // 3. 만료 여부
        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AuthException(AuthErrorCode.EXPIRED_CODE_ERROR);
        }

        // 4. 코드 일치 여부 검증
        boolean matched = securityUtil.matches(inputCode, otp.getCodeHash());
        if (!matched) {
            int remaining = otp.getAttemptsLeft() - 1;
            if (remaining <= 0) {
                passwordMapper.updateOtpAsUsed(email); // 실패 누적 → 사용 불가
                throw new AuthException(AuthErrorCode.OTP_INVALID);
            } else {
                passwordMapper.updateAttempts(email, remaining);
                throw new AuthException(AuthErrorCode.OTP_INVALID_ATTEMPT);
            }
        }

        // 5. 성공 처리: OTP 사용 처리 및 비밀번호 재설정 토큰 발급
        passwordMapper.updateOtpAsUsed(email);

        String token = UUID.randomUUID().toString();
        // 유효시간은 OTP와 별도로 관리(예: 15분)
        LocalDateTime tokenExpiresAt = LocalDateTime.now().plusMinutes(15);
        int n = passwordMapper.updateResetToken(email, token, false, tokenExpiresAt);

        if (n != 1) {
            throw new AuthException(AuthErrorCode.OTP_INVALID_ATTEMPT);
        }

        return token;
    }

    @Override
    @Transactional
    public void resetPassword(String resetToken, String newRawPassword) {
        // 1) 토큰으로 레코드 조회
        PasswordVerification row = passwordMapper.findByResetToken(resetToken);
        if (row == null) {
            throw new AuthException(AuthErrorCode.RESET_TOKEN_INVALID);
        }

        // 2) 토큰 사용 여부/만료 검사
        if (row.isResetUsed()) {
            throw new AuthException(AuthErrorCode.RESET_TOKEN_ALREADY_USED);
        }
        if (row.getResetExpiresAt() == null || row.getResetExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AuthException(AuthErrorCode.RESET_TOKEN_EXPIRED);
        }

        // 3) 멤버 조회(존재 확인)
        String email = row.getEmail();
        Member member = authRepository.findByEmail(email).orElseThrow(() -> new AuthException(AuthErrorCode.MEMBER_NOT_FOUND));

        // 4) 패스워드 해시 & 저장
        String hashed = securityUtil.encode(newRawPassword);
        int updated = authRepository.updatePasswordByEmail(email, hashed);
        if (updated != 1) {
            throw new AuthException(AuthErrorCode.SERVER_ERROR);
        }

        // 5) 토큰 1회용 처리 + 무효화(선호에 따라 토큰 null 로도)
        int done = passwordMapper.markResetTokenUsed(resetToken);
        if (done != 1) {
            // 여기서 실패하면 롤백 유도
            throw new AuthException(AuthErrorCode.SERVER_ERROR);
        }
    }

    // 대문자+숫자 6자리
    private String generateCode(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder(length);
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(rnd.nextInt(characters.length())));
        }
        return sb.toString();
    }
}
