package com.project.InsightPrep.domain.auth.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class AuthRequest {

    @Builder
    @Getter
    @JsonInclude(Include.NON_NULL)
    @NoArgsConstructor
    @AllArgsConstructor
    public static class signupDto {

        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        private String email;

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*[@$!%*?&]).{8,16}$", message = "비밀번호는 영문 소문자, 대문자, 특수 문자로 구성되어야 합니다.")
        private String password;

        @NotBlank(message = "비밀번호를 다시 입력해주세요.")
        private String re_password;

        @NotBlank(message = "닉네임은 필수입니다.")
        @Pattern(regexp = "^[가-힣a-zA-Z0-9]+$", message = "닉네임은 한글,영문,숫자만 사용할 수 있습니다.")
        @Size(max = 10, message = "닉네임은 10자 이내여야 합니다.")
        private String nickname;
    }

    @Getter
    @NoArgsConstructor
    public static class MemberEmailDto {
        @NotBlank
        @Email
        private String email;
    }

    @Getter
    @NoArgsConstructor
    @Setter
    public static class MemberEmailVerifyDto {
        @NotBlank
        private String email;

        @NotBlank
        private String code;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginDto {
        @NotBlank @Email
        private String email;

        @NotBlank
        private String password;

        private boolean autoLogin;
    }
}
