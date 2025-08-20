package com.project.InsightPrep.domain.question.exception;

import com.project.InsightPrep.global.common.response.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum QuestionErrorCode implements BaseErrorCode {

    FEEDBACK_NOT_FOUND("FEEDBACK_NOT_FOUND", HttpStatus.NOT_FOUND, "해당 답변의 피드백이 존재하지 않습니다."),
    QUESTION_NOT_FOUND("QUESTION_NOT_FOUND", HttpStatus.NOT_FOUND, "해당 질문이 존재하지 않습니다."),
    ALREADY_DELETED("ALREADY_DELETED", HttpStatus.NOT_FOUND, "이미 삭제되었거나 찾을 수 없습니다."),
    ANSWER_NOT_FOUND("ANSWER_NOT_FOUND", HttpStatus.NOT_FOUND, "답변을 찾을 수 없습니다.");


    private final String code;
    private final HttpStatus status;
    private final String message;
}
