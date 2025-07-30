package com.project.InsightPrep.global.auth.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.project.InsightPrep.domain.member.entity.Member;
import com.project.InsightPrep.domain.member.entity.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

class CustomUserDetailsTest {

    @Test
    @DisplayName("CustomUserDetails 필드 및 메서드 검증")
    void customUserDetails_methods_shouldReturnCorrectValues() {
        // given
        Member member = Member.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPassword")
                .nickname("nickname")
                .role(Role.USER)
                .build();

        CustomUserDetails userDetails = new CustomUserDetails(member);

        // when & then
        assertThat(userDetails.getUsername()).isEqualTo("test@example.com");
        assertThat(userDetails.getPassword()).isEqualTo("encodedPassword");
        assertThat(userDetails.getMember()).isEqualTo(member);
        assertThat(userDetails.isAccountNonExpired()).isTrue();
        assertThat(userDetails.isAccountNonLocked()).isTrue();
        assertThat(userDetails.isCredentialsNonExpired()).isTrue();
        assertThat(userDetails.isEnabled()).isTrue();

        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertThat(authorities).hasSize(1);
        assertThat(authorities.iterator().next())
                .isEqualTo(new SimpleGrantedAuthority("ROLE_USER"));
    }
}