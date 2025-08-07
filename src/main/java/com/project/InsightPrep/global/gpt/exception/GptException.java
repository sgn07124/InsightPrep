package com.project.InsightPrep.global.gpt.exception;

import com.project.InsightPrep.global.common.response.code.BaseErrorCode;
import com.project.InsightPrep.global.exception.CustomException;

public class GptException extends CustomException {

    public GptException(BaseErrorCode errorCode) {
        super(errorCode);
    }
}