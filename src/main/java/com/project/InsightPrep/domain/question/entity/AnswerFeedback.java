package com.project.InsightPrep.domain.question.entity;

import com.project.InsightPrep.global.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "answer_feedback")
public class AnswerFeedback extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "answer_id", nullable = false)
    private Answer answer;

    private Integer score;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String summary;  // 간단한 요약

    @Column(columnDefinition = "TEXT", nullable = false)
    private String improvement;  // 개선점 제안
}
