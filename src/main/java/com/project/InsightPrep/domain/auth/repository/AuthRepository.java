package com.project.InsightPrep.domain.auth.repository;

import com.project.InsightPrep.domain.member.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuthRepository extends JpaRepository<Member, Long> {

    // 이메일 중복 여부 확인
    boolean existsByEmail(String email);

    // 이메일로 회원 조회
    Optional<Member> findByEmail(String email);

    // ID로 회원 조회
    Optional<Member> findById(Long id);

    // 이메일로 비밀번호 변경 (Update 쿼리)
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Member m SET m.password = :passwordHash WHERE m.email = :email")
    int updatePasswordByEmail(@Param("email") String email, @Param("passwordHash") String passwordHash);
}
