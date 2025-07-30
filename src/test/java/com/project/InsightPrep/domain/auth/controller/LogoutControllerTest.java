package com.project.InsightPrep.domain.auth.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class LogoutControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("로그아웃 실패 - 인증되지 않은 사용자")
    void logout_fail_unauthenticated() throws Exception {
        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isUnauthorized()) // 401
                .andExpect(jsonPath("$.code").value("NEED_LOGIN_ERROR"))
                .andExpect(jsonPath("$.message").value("로그인이 필요한 요청입니다."));
    }
}
