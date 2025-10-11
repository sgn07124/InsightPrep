package com.project.InsightPrep.global.auth.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

import com.project.InsightPrep.domain.auth.exception.AuthErrorCode;
import com.project.InsightPrep.domain.auth.exception.AuthException;
import com.project.InsightPrep.domain.auth.mapper.AuthMapper;
import com.project.InsightPrep.domain.auth.repository.AuthRepository;
import com.project.InsightPrep.domain.member.entity.Member;
import com.project.InsightPrep.domain.member.entity.Role;
import com.project.InsightPrep.global.auth.domain.CustomUserDetails;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class SecurityUtilTest {

    @InjectMocks
    private SecurityUtil securityUtil;

    @Mock
    private AuthMapper authMapper;

    @Mock
    private AuthRepository authRepository;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("인증된 사용자의 ID를 정상적으로 반환한다")
    void getLoginMemberId_success() {
        // given
        Member member = Member.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPassword")
                .nickname("nickname")
                .role(Role.USER)
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(member);
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // when
        Long loginMemberId = securityUtil.getLoginMemberId();

        // then
        assertEquals(1L, loginMemberId);
    }

    @Test
    @DisplayName("인증되지 않은 사용자일 경우 예외 발생")
    void getLoginMemberId_fail_unauthenticated() {
        // given
        SecurityContextHolder.getContext().setAuthentication(null);

        // when & then
        AuthException exception = assertThrows(AuthException.class, () -> {
            securityUtil.getLoginMemberId();
        });

        assertEquals(AuthErrorCode.NOT_AUTHENTICATED, exception.getErrorCode());
    }

    @Test
    @DisplayName("인증 principal이 CustomUserDetails가 아닐 경우 예외 발생")
    void getLoginMemberId_fail_invalidPrincipal() {
        // given
        Authentication auth = new UsernamePasswordAuthenticationToken("anonymousUser", null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        // when & then
        AuthException exception = assertThrows(AuthException.class, () -> {
            securityUtil.getLoginMemberId();
        });

        assertEquals(AuthErrorCode.INVALID_AUTHENTICATION_PRINCIPAL, exception.getErrorCode());
    }

    @Test
    @DisplayName("인증된 사용자의 Member 객체를 반환한다")
    void getAuthenticatedMember_success() {
        // given
        Member member = Member.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPassword")
                .nickname("nickname")
                .role(Role.USER)
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(member);
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        given(authRepository.findById(1L)).willReturn(Optional.of(member));

        // when
        Member result = securityUtil.getAuthenticatedMember();

        // then
        assertEquals(member.getEmail(), result.getEmail());
    }

    @Test
    @DisplayName("인증된 사용자의 Member가 DB에 없으면 예외 발생")
    void getAuthenticatedMember_fail_memberNotFound() {
        // given
        Member member = Member.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPassword")
                .nickname("nickname")
                .role(Role.USER)
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(member);
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        given(authRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        AuthException exception = assertThrows(AuthException.class, () -> {
            securityUtil.getAuthenticatedMember();
        });

        assertEquals(AuthErrorCode.MEMBER_NOT_FOUND, exception.getErrorCode());
    }
}