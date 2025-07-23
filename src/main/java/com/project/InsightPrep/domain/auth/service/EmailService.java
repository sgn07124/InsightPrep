package com.project.InsightPrep.domain.auth.service;

import jakarta.mail.MessagingException;
import org.springframework.mail.SimpleMailMessage;

public interface EmailService {

    void sendEmail(String toEmail, String title, String content) throws MessagingException;

    SimpleMailMessage createEmailForm(String toEmail, String title, String text);

    void sendCodeToEmail(String email);

    boolean verifyCode(String email, String code);

    public void deleteExpiredVerificationCodes();

    public void validateEmailVerified(String email);

    void existEmail(String email);
}
