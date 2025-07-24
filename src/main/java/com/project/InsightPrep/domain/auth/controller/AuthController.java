package com.project.InsightPrep.domain.auth.controller;

import com.project.InsightPrep.domain.auth.controller.docs.AuthControllerDocs;
import com.project.InsightPrep.domain.auth.dto.request.AuthRequest;
import com.project.InsightPrep.domain.auth.dto.response.AuthResponse.LoginResultDto;
import com.project.InsightPrep.domain.auth.exception.AuthErrorCode;
import com.project.InsightPrep.domain.auth.exception.AuthException;
import com.project.InsightPrep.domain.auth.service.AuthService;
import com.project.InsightPrep.domain.auth.service.EmailService;
import com.project.InsightPrep.global.common.response.ApiResponse;
import com.project.InsightPrep.global.common.response.code.ApiSuccessCode;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController implements AuthControllerDocs {

    private final AuthService authService;
    private final EmailService emailService;

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<?>> signup(@RequestBody @Valid AuthRequest.signupDto request) {
        authService.signup(request);
        return ResponseEntity.ok(ApiResponse.of(ApiSuccessCode.SIGNUP_SUCCESS));
    }

    @Override
    @PostMapping("/sendEmail")
    public ResponseEntity<ApiResponse<?>> sendEmail(@RequestBody @Valid AuthRequest.MemberEmailDto emailDto) {
        emailService.sendCodeToEmail(emailDto.getEmail());
        return ResponseEntity.ok(ApiResponse.of(ApiSuccessCode.SEND_EMAIL_SUCCESS));
    }

    @Override
    @PostMapping("/verifyEmail")
    public ResponseEntity<ApiResponse<?>> verifyEmail(@RequestBody @Valid AuthRequest.MemberEmailVerifyDto verifyDto) {
        boolean isVerified = emailService.verifyCode(verifyDto.getEmail(), verifyDto.getCode());

        if (isVerified) {
            return ResponseEntity.ok(ApiResponse.of(ApiSuccessCode.VERIFIED_EMAIL_SUCCESS));
        } else {
            throw new AuthException(AuthErrorCode.CODE_NOT_MATCH_ERROR);
        }
    }

    @Override
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResultDto>> login (@RequestBody @Valid AuthRequest.LoginDto request, HttpSession session) {
        LoginResultDto res = authService.login(request, session);
        return ResponseEntity.ok(ApiResponse.of(ApiSuccessCode.LOGIN_SUCCESS, res));
    }

    @Override
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logout (HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession();
        if (session != null) {
            session.invalidate();
        }

        // JSESSIONID 쿠키 삭제
        Cookie cookie = new Cookie("JSESSIONID", null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return ResponseEntity.ok(ApiResponse.of(ApiSuccessCode.LOGOUT_SUCCESS));
    }

    @GetMapping("/debug/session")
    public ResponseEntity<?> debugSession(HttpSession session) {
        String sessionId = session.getId();  // 현재 클라이언트가 가진 세션 ID
        int sessionExpires = session.getMaxInactiveInterval();  // 세션 만료 시간 (쿠키는 임시 쿠키이므로 max-age 정보가 없음)
        Object memberId = session.getAttribute("LOGIN_MEMBER_ID");  // 세션에 저장된 사용자 정보

        Map<String, Object> debugInfo = new HashMap<>();
        debugInfo.put("sessionId", sessionId);
        debugInfo.put("sessionExpires", sessionExpires);
        debugInfo.put("loginMemberId", memberId);

        return ResponseEntity.ok(debugInfo);
    }
}
