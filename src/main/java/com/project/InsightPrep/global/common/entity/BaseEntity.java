package com.project.InsightPrep.global.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class BaseEntity extends BaseTimeEntity {

    @CreatedBy
    @Column(nullable = false, updatable = false)
    private Long createdBy;

    @LastModifiedBy
    @Column(nullable = false)
    private Long updatedBy;
}
