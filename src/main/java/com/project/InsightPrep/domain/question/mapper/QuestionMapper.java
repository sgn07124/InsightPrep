package com.project.InsightPrep.domain.question.mapper;

import com.project.InsightPrep.domain.question.entity.Question;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QuestionMapper {
    void insertQuestion(Question question);
}