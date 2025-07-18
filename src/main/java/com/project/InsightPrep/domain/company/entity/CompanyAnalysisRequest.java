package com.project.InsightPrep.domain.company.entity;

import com.project.InsightPrep.domain.member.entity.Member;
import com.project.InsightPrep.global.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "company_analysis_request")
public class CompanyAnalysisRequest extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;  // 요청 대상 기업

    @ManyToOne
    @JoinColumn(name = "requested_by", nullable = false)
    private Member requestedBy;  // 요청한 사용자

    @ManyToOne
    @JoinColumn(name = "reviewed_by")
    private Member reviewedBy;  // 관리자

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status; // PENDING, APPROVED, REJECTED

    @Column(length = 255)
    private String reason; // 거절 사유 등

}
