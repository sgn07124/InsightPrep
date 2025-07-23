package com.project.InsightPrep.domain.auth.controller.docs;

import com.project.InsightPrep.domain.auth.dto.request.AuthRequest;
import com.project.InsightPrep.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

}
