package com.project.InsightPrep.domain.auth.mapper;

import com.project.InsightPrep.domain.member.entity.Member;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuthMapper {

    void insertMember(Member member);

    boolean existEmail(String email);
}
