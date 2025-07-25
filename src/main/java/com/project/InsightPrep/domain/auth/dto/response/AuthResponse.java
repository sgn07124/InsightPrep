package com.project.InsightPrep.domain.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.project.InsightPrep.domain.member.entity.Role;
import lombok.Builder;
import lombok.Getter;

public class AuthResponse {

    @Getter
    @Builder
    @JsonInclude(Include.NON_NULL)
    public static class SignupResultDto {

        private long id;
        private String email;
        private String nickname;
        private Role role;

    }
}
