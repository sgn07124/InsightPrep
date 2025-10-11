package com.project.InsightPrep.global.auth.util;

import com.project.InsightPrep.domain.auth.exception.AuthErrorCode;
import com.project.InsightPrep.domain.auth.exception.AuthException;
import com.project.InsightPrep.domain.auth.mapper.AuthMapper;
import com.project.InsightPrep.domain.auth.repository.AuthRepository;
import com.project.InsightPrep.global.auth.domain.CustomUserDetails;
import com.project.InsightPrep.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;

@Component
@RequiredArgsConstructor
public class SecurityUtil {

    private final AuthMapper authMapper;
    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;

    public Long getLoginMemberId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthException(AuthErrorCode.NOT_AUTHENTICATED);
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails.getMember().getId();
        }

        throw new AuthException(AuthErrorCode.INVALID_AUTHENTICATION_PRINCIPAL);
    }

    public Member getAuthenticatedMember() {
        Long memberId = getLoginMemberId();
        return authRepository.findById(memberId)
                .orElseThrow(() -> new AuthException(AuthErrorCode.MEMBER_NOT_FOUND));
    }

    /**
     * Compare a raw value with an encoded hash using the configured PasswordEncoder.
     * Returns false if either argument is null.
     */
    public boolean matches(String raw, String encoded) {
        if (raw == null || encoded == null) {
            return false;
        }
        return passwordEncoder.matches(raw, encoded);
    }

    /**
     * Encode a raw value using the configured PasswordEncoder.
     * @param raw the plain text value to encode (must not be null)
     * @return encoded hash
     * @throws IllegalArgumentException if raw is null
     */
    public String encode(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("raw must not be null");
        }
        return passwordEncoder.encode(raw);
    }
}
