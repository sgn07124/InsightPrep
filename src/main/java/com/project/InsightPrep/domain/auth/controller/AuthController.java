package com.project.InsightPrep.domain.auth.controller;

import com.project.InsightPrep.domain.auth.dto.request.AuthRequest;
import com.project.InsightPrep.domain.auth.service.AuthService;
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

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<?>> signup(@RequestBody @Valid AuthRequest.signupDto request) {
        authService.signup(request);
        return ResponseEntity.ok(ApiResponse.of(ApiSuccessCode.SIGNUP_SUCCESS));
    }
}
