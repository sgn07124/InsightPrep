package com.project.InsightPrep.domain.company.entity;

import com.project.InsightPrep.global.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "company_analysis_section")
public class CompanyAnalysisSection extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false, length = 100)
    private String sectionKey; // ex) summary_3lines

    @Column(nullable = false)
    private String sectionTitle; // ex) 3줄 요약

    @Lob
    @Column(nullable = false)
    private String content; // JSON 문자열 or 텍스트
}
