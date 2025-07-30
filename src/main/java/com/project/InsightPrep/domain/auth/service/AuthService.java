package com.project.InsightPrep.domain.auth.service;

import com.project.InsightPrep.domain.auth.dto.request.AuthRequest.LoginDto;
import com.project.InsightPrep.domain.auth.dto.request.AuthRequest.signupDto;
import com.project.InsightPrep.domain.auth.dto.response.AuthResponse.LoginResultDto;
import com.project.InsightPrep.domain.member.entity.Member;
import jakarta.servlet.http.HttpSession;

public interface AuthService {
    void signup(signupDto request);

    LoginResultDto login(LoginDto request);

    Member findById(Long id);
}
