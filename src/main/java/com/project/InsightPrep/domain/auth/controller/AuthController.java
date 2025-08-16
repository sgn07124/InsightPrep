package com.project.InsightPrep.domain.auth.controller;

import com.project.InsightPrep.domain.auth.controller.docs.AuthControllerDocs;
import com.project.InsightPrep.domain.auth.dto.request.AuthRequest;
import com.project.InsightPrep.domain.auth.dto.request.AuthRequest.ResetTokenRes;
import com.project.InsightPrep.domain.auth.dto.response.AuthResponse.LoginResultDto;
import com.project.InsightPrep.domain.auth.dto.response.AuthResponse.MeDto;
import com.project.InsightPrep.domain.auth.exception.AuthErrorCode;
import com.project.InsightPrep.domain.auth.exception.AuthException;
import com.project.InsightPrep.domain.auth.service.AuthService;
import com.project.InsightPrep.domain.auth.service.EmailService;
import com.project.InsightPrep.domain.auth.service.PasswordResetService;
import com.project.InsightPrep.global.common.response.ApiResponse;
import com.project.InsightPrep.global.common.response.code.ApiSuccessCode;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private final PasswordResetService passwordResetService;

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
    public ResponseEntity<ApiResponse<LoginResultDto>> login (@RequestBody @Valid AuthRequest.LoginDto request) {
        LoginResultDto res = authService.login(request);
        return ResponseEntity.ok(ApiResponse.of(ApiSuccessCode.LOGIN_SUCCESS, res));
    }

    @Override
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
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

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MeDto>> getSessionInfo(HttpSession session) {
        MeDto dto = authService.getSessionInfo(session);
        return ResponseEntity.ok(ApiResponse.of(ApiSuccessCode.ME_SUCCESS, dto));
    }

    @Override
    @PostMapping("/opt/sendEmail")
    public ResponseEntity<ApiResponse<Void>> requestOtp(@RequestBody @Valid AuthRequest.MemberEmailDto req) {
        passwordResetService.requestOtp(req.getEmail());
        return ResponseEntity.ok(ApiResponse.of(ApiSuccessCode.SEND_EMAIL_SUCCESS)); // 존재 여부 노출 금지
    }

    @Override
    @PostMapping("/otp/verify")
    public ResponseEntity<ApiResponse<ResetTokenRes>> verifyOtp(@RequestBody @Valid AuthRequest.VerifyOtpReq req) {
        String token = passwordResetService.verifyOtp(req.email(), req.code());
        return ResponseEntity.ok(ApiResponse.of(ApiSuccessCode.VERIFIED_EMAIL_SUCCESS, new ResetTokenRes(token)));
    }

    @Override
    @PostMapping("/password/reset")
    public ResponseEntity<ApiResponse<Void>> reset(@RequestBody @Valid AuthRequest.ResetReq req) {
        passwordResetService.resetPassword(req.resetToken(), req.newPassword());
        return ResponseEntity.ok(ApiResponse.of(ApiSuccessCode.SUCCESS));
    }
}
