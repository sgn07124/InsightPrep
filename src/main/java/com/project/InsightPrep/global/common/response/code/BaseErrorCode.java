package com.project.InsightPrep.global.common.response.code;

import org.springframework.http.HttpStatus;

public interface BaseErrorCode {

    String getCode();
    HttpStatus getStatus();
    String getMessage();
}
