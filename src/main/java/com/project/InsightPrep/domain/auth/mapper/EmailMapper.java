package com.project.InsightPrep.domain.auth.mapper;

import com.project.InsightPrep.domain.auth.entity.EmailVerification;
import java.time.LocalDateTime;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface EmailMapper {

    void insertCode(EmailVerification code);

    Optional<EmailVerification> findByEmailAndCode(@Param("email") String email, @Param("code") String code);

    void deleteByEmail(String email);

    EmailVerification findByEmail(String email);

    void deleteByExpiresTimeBefore(LocalDateTime expiresTime);

    void updateVerified(@Param("email") String email, @Param("code") String code);
}
