package com.project.InsightPrep.domain.auth.controller;

import com.project.InsightPrep.domain.auth.controller.docs.AuthControllerDocs;
import com.project.InsightPrep.domain.auth.dto.request.AuthRequest;
import com.project.InsightPrep.domain.auth.exception.AuthErrorCode;
import com.project.InsightPrep.domain.auth.exception.AuthException;
import com.project.InsightPrep.domain.auth.service.AuthService;
import com.project.InsightPrep.domain.auth.service.EmailService;
import com.project.InsightPrep.global.common.response.ApiResponse;
import com.project.InsightPrep.global.common.response.code.ApiSuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ApiResponse<String>> sendEmail(@RequestBody @Valid AuthRequest.MemberEmailDto emailDto) {
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
}
