package com.project.InsightPrep.domain.auth.mapper;

import java.time.LocalDateTime;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

@Mapper
public interface PasswordMapper {

    void upsertPasswordOtp(@Param("email") String email,
                           @Param("codeHash") String codeHash,
                           @Param("attemptsLeft") int attemptsLeft,
                           @Param("used") boolean used,
                           @Param("expiresAt") LocalDateTime expiresAt,
                           @Param("createdAt") LocalDateTime createdAt);
}
