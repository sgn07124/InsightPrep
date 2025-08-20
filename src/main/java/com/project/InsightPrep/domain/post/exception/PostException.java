package com.project.InsightPrep.domain.post.exception;

import com.project.InsightPrep.global.common.response.code.BaseErrorCode;
import com.project.InsightPrep.global.exception.CustomException;
import lombok.Getter;

@Getter
public class PostException extends CustomException {

    public PostException(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
