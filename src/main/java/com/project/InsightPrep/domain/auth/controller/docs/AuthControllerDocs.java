package com.project.InsightPrep.domain.auth.controller.docs;

import com.project.InsightPrep.domain.auth.dto.request.AuthRequest;
import com.project.InsightPrep.domain.auth.dto.response.AuthResponse.LoginResultDto;
import com.project.InsightPrep.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Auth", description = "Auth 관련 API")
public interface AuthControllerDocs {

    @Operation(summary = "회원가입", description = "회원가입 로직을 처리합니다.")
    public ResponseEntity<ApiResponse<?>> signup(@RequestBody @Valid AuthRequest.signupDto request);

    @Operation(summary = "이메일 인증 번호 전송", description = "이메일 인증을 위해 인증 번호를 해당 메일로 전송합니다.")
    public ResponseEntity<ApiResponse<?>> sendEmail(@RequestBody @Valid AuthRequest.MemberEmailDto request);

    @Operation(summary = "이메일 인증", description = "이메일과 인증 코드로 인증을 진행합니다. 인증 코드의 만료 시간은 10분 입니다.")
    public ResponseEntity<ApiResponse<?>> verifyEmail(@RequestBody @Valid AuthRequest.MemberEmailVerifyDto request);

    @Operation(summary = "로그인", description = "이메일, 비밀번호로 로그인을 합니다. ")
    public ResponseEntity<ApiResponse<LoginResultDto>> login (@RequestBody @Valid AuthRequest.LoginDto request, HttpSession session);

    @Operation(summary = "로그아웃", description = "로그아웃을 진행하면 쿠키가 삭제됩니다.")
    public ResponseEntity<ApiResponse<?>> logout (HttpServletRequest request, HttpServletResponse response);
}
