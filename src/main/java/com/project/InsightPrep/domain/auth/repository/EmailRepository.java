package com.project.InsightPrep.domain.auth.repository;

import com.project.InsightPrep.domain.auth.entity.EmailVerification;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmailRepository extends JpaRepository<EmailVerification, Long> {

    // 이메일과 코드로 조회
    Optional<EmailVerification> findByEmailAndCode(String email, String code);

    //이메일로 조회
    Optional<EmailVerification> findByEmail(String email);

    // 이메일로 삭제
    void deleteByEmail(String email);

    // 만료 시간 이전 데이터 삭제
    void deleteByExpiresTimeBefore(LocalDateTime expiresTime);

    // 인증 성공 시 verified = true 로 변경. Dirty Checking보다는 단일 업데이트 쿼리로 처리
    @Modifying(clearAutomatically = true)
    @Query("UPDATE EmailVerification e SET e.verified = true WHERE e.email = :email AND e.code = :code")
    int updateVerified(@Param("email") String email, @Param("code") String code);
}
