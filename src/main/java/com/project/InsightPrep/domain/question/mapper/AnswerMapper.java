package com.project.InsightPrep.domain.question.mapper;

import com.project.InsightPrep.domain.question.entity.Answer;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AnswerMapper {
    void insertAnswer(Answer answer);
}