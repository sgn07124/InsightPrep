package com.project.InsightPrep.domain.auth.mapper;

import com.project.InsightPrep.domain.auth.entity.PasswordVerification;
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

    PasswordVerification findByEmail(@Param("email") String email);

    int updateAttempts(@Param("email") String email,
                       @Param("attemptsLeft") int attemptsLeft);

    int updateOtpAsUsed(@Param("email") String email);

    int updateResetToken(@Param("email") String email,
                         @Param("resetToken") String resetToken,
                         @Param("resetUsed") boolean resetUsed,
                         @Param("resetExpiresAt") LocalDateTime resetExpiresAt);
}
