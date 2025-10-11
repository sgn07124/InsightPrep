package com.project.InsightPrep.domain.auth.controller;

import com.project.InsightPrep.global.auth.domain.CustomUserDetails;
import com.project.InsightPrep.domain.member.entity.Member;
import com.project.InsightPrep.global.auth.util.SecurityUtil;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class TestController {

    private final SecurityUtil securityUtil;

        /*
    - 컨트롤러에서 사용자 ID 등을 사용할 경우, "@AuthenticationPrincipal CustomUserDetails userDetails" 추가. 단, Service 단의 코드의 매개변수로 넘겨야됨.
    - Service에서 SecurityUtil 사용 (이거 적용) - Controller에 @AuthenticationPrincipal CustomUserDetails userDetails 없어도 됨.
     */

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/debug/session")
    public ResponseEntity<?> debugSession(@AuthenticationPrincipal CustomUserDetails userDetails, HttpSession session) {  //
        String sessionId = session.getId();
        int sessionExpires = session.getMaxInactiveInterval();
        Long memberId = (userDetails != null) ? userDetails.getMember().getId() : null;

        Map<String, Object> debugInfo = new HashMap<>();
        debugInfo.put("sessionId", sessionId);
        debugInfo.put("sessionExpires", sessionExpires);
        debugInfo.put("loginMemberId", memberId);

        return ResponseEntity.ok(debugInfo);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/test/secure")
    public String testSecure(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return "로그인한 사용자 ID: " + userDetails.getMember().getId();
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/test/me")
    public ResponseEntity<?> getMe() {
        Member member = securityUtil.getAuthenticatedMember();
        Map<String, Object> info = new HashMap<>();
        info.put("id", member.getId());
        info.put("email", member.getEmail());
        info.put("nickname", member.getNickname());
        info.put("role", member.getRole());
        return ResponseEntity.ok(info);
    }
}