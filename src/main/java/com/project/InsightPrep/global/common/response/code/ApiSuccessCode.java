package com.project.InsightPrep.global.common.response.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ApiSuccessCode {

    SUCCESS("SUCCESS", HttpStatus.OK, "요청이 성공했습니다."),

    SIGNUP_SUCCESS("SIGNUP_SUCCESS", HttpStatus.OK, "회원가입이 완료되었습니다."),
    SEND_EMAIL_SUCCESS("SEND_EMAIL_SUCCESS", HttpStatus.OK, "이메일 전송 성공"),
    VERIFIED_EMAIL_SUCCESS("VERIFIED_EMAIL_SUCCESS", HttpStatus.OK, "이메일 인증 성공"),
    LOGIN_SUCCESS("LOGIN_SUCCESS", HttpStatus.OK, "로그인 성공"),
    LOGOUT_SUCCESS("LOGOUT_SUCCESS", HttpStatus.OK, "로그아웃 성공"),

    CREATE_QUESTION_SUCCESS("CREATE_QUESTION_SUCCESS", HttpStatus.OK, "질문 생성 성공"),
    SAVE_ANSWER_SUCCESS("SAVE_ANSWER_SUCCESS", HttpStatus.OK, "답변 저장 성공"),
    GET_FEEDBACK_SUCCESS("GET_FEEDBACK_SUCCESS", HttpStatus.OK, "피드백 조회 성공"),
    FEEDBACK_PENDING("FEEDBACK_PENDING", HttpStatus.ACCEPTED, "피드백 생성 중입니다."),
    GET_QUESTIONS_SUCCESS("GET_QUESTIONS_SUCCESS", HttpStatus.OK, "질문 리스트 조회 성공"),
    DELETE_QUESTION_SUCCESS("DELETE_QUESTION_SUCCESS", HttpStatus.OK, "질문과 답변, 피드백 삭제 성공"),
    ME_SUCCESS("ME_SUCCESS", HttpStatus.OK, "로그인 상태입니다.");

    private final String code;
    private final HttpStatus status;
    private final String message;
}
