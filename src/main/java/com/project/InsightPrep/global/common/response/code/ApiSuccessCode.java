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
    ME_SUCCESS("ME_SUCCESS", HttpStatus.OK, "로그인 상태입니다."),

    CREATE_POST_SUCCESS("CREATE_POST_SUCCESS", HttpStatus.OK, "글 생성 성공"),
    GET_POST_SUCCESS("GET_POST_SUCCESS", HttpStatus.OK, "글 조회 성공"),
    UPDATE_POST_STATUS_SUCCESS("UPDATE_POST_STATUS_SUCCESS", HttpStatus.OK, "글 상태 변경 성공"),
    GET_POSTS_SUCCESS("GET_POSTS_SUCCESS", HttpStatus.OK, "글 리스트 조회 성공"),
    CREATE_COMMENT_SUCCESS("CREATE_COMMENT_SUCCESS", HttpStatus.OK, "댓글 저장 성공"),
    UPDATE_COMMENT_SUCCESS("UPDATE_COMMENT_SUCCESS", HttpStatus.OK, "댓글 수정 성공"),
    DELETE_COMMENT_SUCCESS("DELETE_COMMENT_SUCCESS", HttpStatus.OK, "댓글 삭제 성공"),
    GET_COMMENTS_SUCCESS("GET_COMMENTS_SUCCESS", HttpStatus.OK, "댓글 리스트 조회 성공"),
    GET_PREVIEW_SUCCESS("GET_PREVIEW_SUCCESS", HttpStatus.OK, "프리뷰 조회 성공");

    private final String code;
    private final HttpStatus status;
    private final String message;
}
