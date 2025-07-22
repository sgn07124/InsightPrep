package com.project.InsightPrep.domain.auth.service;

import com.project.InsightPrep.domain.auth.dto.request.AuthRequest.signupDto;

public interface AuthService {
    void signup(signupDto request);
}
