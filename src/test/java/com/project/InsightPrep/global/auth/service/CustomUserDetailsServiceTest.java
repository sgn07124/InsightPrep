package com.project.InsightPrep.global.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

import com.project.InsightPrep.domain.auth.mapper.AuthMapper;
import com.project.InsightPrep.domain.member.entity.Member;
import com.project.InsightPrep.domain.member.entity.Role;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private AuthMapper authMapper;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("정상적으로 사용자 정보를 불러오는 경우")
    void loadUserByUsername_success() {
        // given
        String email = "test@example.com";
        Member member = Member.builder()
                .id(1L)
                .email(email)
                .password("encodedPassword")
                .nickname("nickname")
                .role(Role.USER)
                .build();

        given(authMapper.findByEmail(email)).willReturn(Optional.of(member));

        // when
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        // then
        assertNotNull(userDetails);
        assertEquals(email, userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
    }

    @Test
    @DisplayName("사용자가 존재하지 않을 경우 예외 발생")
    void loadUserByUsername_fail_userNotFound() {
        // given
        String email = "nonexistent@example.com";
        given(authMapper.findByEmail(email)).willReturn(Optional.empty());

        // when & then
        assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername(email);
        });
    }
}