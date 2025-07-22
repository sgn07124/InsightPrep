package com.project.InsightPrep.domain.auth.service;

import com.project.InsightPrep.domain.auth.dto.request.AuthRequest.signupDto;
import com.project.InsightPrep.domain.auth.exception.AuthErrorCode;
import com.project.InsightPrep.domain.auth.exception.AuthException;
import com.project.InsightPrep.domain.auth.mapper.AuthMapper;
import com.project.InsightPrep.domain.member.entity.Member;
import com.project.InsightPrep.domain.member.entity.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final PasswordEncoder passwordEncoder;
    private final AuthMapper authMapper;

    @Override
    @Transactional
    public void signup(signupDto dto) {
        if (!dto.isCheckEmail()) {
            throw new AuthException(AuthErrorCode.EMAIL_VERIFICATION_ERROR);
        }

        if (!dto.getPassword().equals(dto.getRe_password())) {
            throw new AuthException(AuthErrorCode.PASSWORD_MATCH_ERROR);
        }

        Member member = Member.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .nickname(dto.getNickname())
                .role(Role.USER)
                .build();

        authMapper.insertMember(member);
    }
}
