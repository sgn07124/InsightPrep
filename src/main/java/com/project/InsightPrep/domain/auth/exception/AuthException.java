package com.project.InsightPrep.domain.auth.exception;

import com.project.InsightPrep.global.common.response.code.BaseErrorCode;
import com.project.InsightPrep.global.exception.CustomException;
import lombok.Getter;

@Getter
public class AuthException extends CustomException {

    public AuthException(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
