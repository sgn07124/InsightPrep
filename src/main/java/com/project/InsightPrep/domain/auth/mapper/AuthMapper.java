package com.project.InsightPrep.domain.auth.mapper;

import com.project.InsightPrep.domain.member.entity.Member;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AuthMapper {

    void insertMember(Member member);

    boolean existEmail(String email);

    Optional<Member> findByEmail(String email);

    Optional<Member> findById(@Param("id") Long id);

    int updatePasswordByEmail(@Param("email") String email,
                              @Param("passwordHash") String passwordHash);
}
