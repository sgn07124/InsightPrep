package com.project.InsightPrep.domain.question.entity;

import com.project.InsightPrep.global.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "question")
public class Question extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO: 관련 기업 - 등록된 기업에 한해서 선택 가능 (기업 선택 관련 기능은 추후 추가 예정)

    @Column(nullable = false)
    private String category; // OS, DB 등

    @Lob
    private String content;
}
