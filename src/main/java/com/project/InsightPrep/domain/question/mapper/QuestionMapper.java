package com.project.InsightPrep.domain.question.mapper;

import com.project.InsightPrep.domain.question.entity.Question;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface QuestionMapper {
    void insertQuestion(Question question);

    Question findById(@Param("id") Long id);
}