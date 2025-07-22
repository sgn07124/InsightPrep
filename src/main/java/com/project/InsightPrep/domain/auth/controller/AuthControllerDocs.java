package com.project.InsightPrep.domain.auth.controller;

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
}
