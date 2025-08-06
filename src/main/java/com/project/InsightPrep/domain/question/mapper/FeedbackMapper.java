package com.project.InsightPrep.domain.question.mapper;

import com.project.InsightPrep.domain.question.entity.AnswerFeedback;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FeedbackMapper {

    void insertFeedback(AnswerFeedback feedback);
}
