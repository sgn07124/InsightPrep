package com.project.InsightPrep.domain.question.exception;

import com.project.InsightPrep.global.common.response.code.BaseErrorCode;
import com.project.InsightPrep.global.exception.CustomException;
import lombok.Getter;

@Getter
public class QuestionException extends CustomException {

    public QuestionException(BaseErrorCode errorCode) {
        super(errorCode);
    }
}