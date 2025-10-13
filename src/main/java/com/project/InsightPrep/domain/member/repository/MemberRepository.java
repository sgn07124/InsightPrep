package com.project.InsightPrep.domain.member.repository;

import com.project.InsightPrep.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
