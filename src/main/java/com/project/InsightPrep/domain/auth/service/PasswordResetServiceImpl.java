package com.project.InsightPrep.domain.auth.service;

import com.project.InsightPrep.domain.auth.entity.PasswordVerification;
import com.project.InsightPrep.domain.auth.exception.AuthErrorCode;
import com.project.InsightPrep.domain.auth.exception.AuthException;
import com.project.InsightPrep.domain.auth.mapper.AuthMapper;
import com.project.InsightPrep.domain.auth.mapper.PasswordMapper;
import com.project.InsightPrep.domain.auth.repository.AuthRepository;
import com.project.InsightPrep.domain.auth.repository.PasswordRepository;
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
    private final PasswordRepository passwordRepository;
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

        //passwordMapper.upsertPasswordOtp(email, codeHash, DEFAULT_ATTEMPTS, false, expiresAt, now);
        PasswordVerification verification = passwordRepository.findByEmail(email)
                .map(existing -> existing.updateOtp(codeHash, DEFAULT_ATTEMPTS, false, expiresAt))
                .orElseGet(() -> PasswordVerification.createNew(email, codeHash, DEFAULT_ATTEMPTS, false, expiresAt));

        passwordRepository.save(verification);

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
        PasswordVerification otp = passwordRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException(AuthErrorCode.CODE_NOT_MATCH_ERROR));

        // 엔티티 스스로 검증 및 상태 변경 수행
        otp.validateOtp(securityUtil, inputCode);

        // OTP 성공 시: 사용 처리 & 비밀번호 재설정 토큰 발급
        otp.markOtpUsed();
        String token = UUID.randomUUID().toString();
        otp.issueResetToken(token, LocalDateTime.now().plusMinutes(15));

        // JPA가 변경 감지 → UPDATE 자동 수행
        return token;
    }

    @Override
    @Transactional
    public void resetPassword(String resetToken, String newRawPassword) {
        // 1) 토큰으로 레코드 조회
        PasswordVerification verification = passwordRepository.findByResetToken(resetToken)
                .orElseThrow(() -> new AuthException(AuthErrorCode.RESET_TOKEN_INVALID));

        // 2) 토큰 사용 여부/만료 검사
        if (verification.isResetUsed()) {
            throw new AuthException(AuthErrorCode.RESET_TOKEN_ALREADY_USED);
        }
        if (verification.getResetExpiresAt() == null || verification.getResetExpiresAt().isBefore(LocalDateTime.now())) {
            throw new AuthException(AuthErrorCode.RESET_TOKEN_EXPIRED);
        }

        // 3) 멤버 조회(존재 확인)
        String email = verification.getEmail();
        Member member = authRepository.findByEmail(email).orElseThrow(() -> new AuthException(AuthErrorCode.MEMBER_NOT_FOUND));

        // 4) 패스워드 해시 & 저장
        try {
            String hashed = securityUtil.encode(newRawPassword);
            member.updatePassword(hashed);
        } catch (RuntimeException e) {
            throw new AuthException(AuthErrorCode.SERVER_ERROR);
        }


        // 5) 토큰 1회용 처리 (도메인 책임)
        verification.markResetTokenUsed(); // JPA dirty checking으로 자동 업데이트
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
