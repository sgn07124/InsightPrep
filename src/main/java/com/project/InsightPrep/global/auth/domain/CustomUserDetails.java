package com.project.InsightPrep.global.auth.domain;

import com.project.InsightPrep.domain.member.entity.Member;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class CustomUserDetails implements UserDetails {

    private final Member member;

    public CustomUserDetails(Member member) {
        this.member = member;
    }

    public Member getMember() {
        return member;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + member.getRole())); // 예: ROLE_USER, ROLE_ADMIN
    }

    @Override
    public String getPassword() {
        return member.getPassword();  // 암호화된 비밀번호
    }

    @Override
    public String getUsername() {
        return member.getEmail();  // 로그인 ID로 사용할 필드
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
