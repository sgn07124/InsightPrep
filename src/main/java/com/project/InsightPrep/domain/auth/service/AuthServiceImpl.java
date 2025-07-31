package com.project.InsightPrep.domain.auth.service;

import com.project.InsightPrep.domain.auth.dto.request.AuthRequest.LoginDto;
import com.project.InsightPrep.domain.auth.dto.request.AuthRequest.signupDto;
import com.project.InsightPrep.domain.auth.dto.response.AuthResponse.LoginResultDto;
import com.project.InsightPrep.domain.auth.exception.AuthErrorCode;
import com.project.InsightPrep.domain.auth.exception.AuthException;
import com.project.InsightPrep.domain.auth.mapper.AuthMapper;
import com.project.InsightPrep.domain.member.entity.Member;
import com.project.InsightPrep.domain.member.entity.Role;
import com.project.InsightPrep.global.auth.domain.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final PasswordEncoder passwordEncoder;
    private final AuthMapper authMapper;
    private final EmailService emailService;

    @Override
    @Transactional
    public void signup(signupDto dto) {
        validateSignUp(dto);

        Member member = Member.builder()
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .nickname(dto.getNickname())
                .role(Role.USER)
                .build();

        authMapper.insertMember(member);
    }

    @Override
    @Transactional
    public LoginResultDto login(LoginDto dto) {
        Member member = authMapper.findByEmail(dto.getEmail()).orElseThrow(() -> new AuthException(AuthErrorCode.LOGIN_FAIL));

        if (!passwordEncoder.matches(dto.getPassword(), member.getPassword())) {
            throw new AuthException(AuthErrorCode.LOGIN_FAIL);
        }

        // 인증 객체 생성
        CustomUserDetails userDetails = new CustomUserDetails(member);
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        // SecurityContext에 인증 정보 저장
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        // 세션 유지 시간 조정
        if (dto.isAutoLogin()) {
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpSession session = attr.getRequest().getSession(false);
            if (session != null) {
                session.setMaxInactiveInterval(7 * 24 * 60 * 60);  // 7일
            }
        }

        // 세션에 SecurityContext를 저장 (세션 기반 인증 유지)
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = attr.getRequest();
        HttpSession session = request.getSession(true);  // true: 없으면 생성

        SecurityContext context = SecurityContextHolder.getContext();
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

        return new LoginResultDto(member.getId(), member.getNickname());
    }

    private void validateSignUp(signupDto dto) {
        emailService.existEmail(dto.getEmail());
        emailService.validateEmailVerified(dto.getEmail());  // 이메일 인증 여부
        validatePasswordMatched(dto);  // 비밀번호 매치 여부
    }

    private static void validatePasswordMatched(signupDto dto) {
        if (!dto.getPassword().equals(dto.getRe_password())) {
            throw new AuthException(AuthErrorCode.PASSWORD_MATCH_ERROR);
        }
    }
}
