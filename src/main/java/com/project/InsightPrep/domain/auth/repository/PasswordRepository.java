package com.project.InsightPrep.domain.auth.repository;

import com.project.InsightPrep.domain.auth.entity.PasswordVerification;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordRepository extends JpaRepository<PasswordVerification, Long> {

    Optional<PasswordVerification> findByEmail(String email);

    Optional<PasswordVerification> findByResetToken(String resetToken);

    boolean existsByEmail(String email);
}
