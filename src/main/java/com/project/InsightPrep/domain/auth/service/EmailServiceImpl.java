package com.project.InsightPrep.domain.auth.service;

import com.project.InsightPrep.domain.auth.entity.EmailVerification;
import com.project.InsightPrep.domain.auth.exception.AuthErrorCode;
import com.project.InsightPrep.domain.auth.exception.AuthException;
import com.project.InsightPrep.domain.auth.mapper.AuthMapper;
import com.project.InsightPrep.domain.auth.mapper.EmailMapper;
import com.project.InsightPrep.domain.auth.repository.AuthRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender emailSender;
    private final AuthMapper authMapper;
    private final AuthRepository authRepository;
    private final EmailMapper emailMapper;

    private static final long EXPIRE_MINUTES = 10;

    @Value("${spring.mail.username}")
    private String username;

    @Override
    @Transactional
    public void sendEmail(String toEmail, String title, String content) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(toEmail);
        helper.setSubject(title);
        helper.setText(content, true);
        helper.setReplyTo(username);
        try {
            emailSender.send(message);
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new RuntimeException("Unable to send in sendEmail", e);
        }
    }

    @Override
    @Transactional
    public SimpleMailMessage createEmailForm(String toEmail, String title, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(title);
        message.setText(text);
        return message;
    }

    @Override
    @Transactional
    public void sendCodeToEmail(String email) {

        existEmail(email);  // 사용자 전체 대상 이메일 중복 검사

        currentEmailExisting(email);  // 기존 인증 코드 존재 시 삭제

        EmailVerification createCode = createVerificationCode(email);

        // 이메일 전송
        String title = "InsightPrep 이메일 인증 번호";

        String content = "<html>"
                + "<body>"
                + "<h1>InsightPrep 인증 코드: " + createCode.getCode() + "</h1>"
                + "<p>해당 코드를 홈페이지에 입력하세요.</p>"
                + "<footer style='color: grey; font-size: small;'>"
                + "<p>※본 메일은 자동응답 메일이므로 본 메일에 회신하지 마시기 바랍니다.</p>"
                + "</footer>"
                + "</body>"
                + "</html>";

        try {
            sendEmail(email, title, content);
        } catch (RuntimeException | MessagingException e) {
            e.printStackTrace();
            throw new RuntimeException("Unable to send email in SendCodeToEmail", e);
        }
    }

    private void currentEmailExisting(String email) {
        EmailVerification currentExisting = emailMapper.findByEmail(email);
        if (currentExisting == null) return;

        if (currentExisting.getExpiresTime() == null || currentExisting.getExpiresTime().isBefore(LocalDateTime.now())) {
            emailMapper.deleteByEmail(email);
        } else {
            throw new AuthException(AuthErrorCode.ALREADY_SEND_CODE_ERROR);
        }
    }

    @Override
    public void existEmail(String email) {
        if (authRepository.existsByEmail(email)) {
            throw new AuthException(AuthErrorCode.EMAIL_DUPLICATE_ERROR);
        }
    }

    private EmailVerification createVerificationCode(String email) {
        String randomCode = generateRandomCode(6);
        EmailVerification code = EmailVerification.builder()
                .email(email)
                .code(randomCode)
                .expiresTime(LocalDateTime.now().plusMinutes(EXPIRE_MINUTES))  // 10분 후 만료
                .build();
        emailMapper.insertCode(code);
        return code;
    }

    private String generateRandomCode(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (int i = 0; i < length; i++) {
            sb.append(characters.charAt(random.nextInt(characters.length())));
        }
        return sb.toString();
    }

    @Override
    public boolean verifyCode(String email, String code) {
        return emailMapper.findByEmailAndCode(email, code)
                .map(vc -> {
                    if (vc.getExpiresTime().isBefore(LocalDateTime.now())) {
                        throw new AuthException(AuthErrorCode.EXPIRED_CODE_ERROR); // 만료된 코드
                    }

                    emailMapper.updateVerified(email, code); // 인증 완료 처리
                    return true;
                })
                .orElseThrow(() -> new AuthException(AuthErrorCode.CODE_NOT_MATCH_ERROR));
    }

    @Transactional
    @Scheduled(cron = "0 0 12 * * ?")
    @Override
    public void deleteExpiredVerificationCodes() {
        emailMapper.deleteByExpiresTimeBefore(LocalDateTime.now());
    }

    @Override
    public void validateEmailVerified(String email) {
        EmailVerification verification = emailMapper.findByEmail(email);

        if (verification == null || !verification.isVerified()) {
            throw new AuthException(AuthErrorCode.EMAIL_VERIFICATION_ERROR);
        }
    }
}
