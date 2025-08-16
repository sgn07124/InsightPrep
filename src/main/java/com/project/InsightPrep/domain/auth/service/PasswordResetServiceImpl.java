package com.project.InsightPrep.domain.auth.service;

import com.project.InsightPrep.domain.auth.mapper.AuthMapper;
import com.project.InsightPrep.domain.auth.mapper.PasswordMapper;
import jakarta.mail.MessagingException;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private static final long EXPIRE_MINUTES = 10L; // 10분
    private static final int DEFAULT_ATTEMPTS = 5;

    private final EmailService emailService;
    private final PasswordMapper passwordMapper;
    private final AuthMapper authMapper;

    @Override
    public void requestOtp(String email) {
        boolean exists = authMapper.existEmail(email);
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
    public String verifyOtp(String email, String code) {
        return null;
    }

    @Override
    public void resetPassword(String s, String s1) {

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
