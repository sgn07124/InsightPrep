package com.project.InsightPrep.domain.auth.service;

public interface PasswordResetService {
    void requestOtp(String email);

    String verifyOtp(String email, String code);

    void resetPassword(String s, String s1);
}
