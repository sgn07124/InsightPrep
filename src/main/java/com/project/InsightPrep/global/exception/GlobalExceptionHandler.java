package com.project.InsightPrep.global.exception;

import com.project.InsightPrep.global.common.response.ApiErrorResponse;
import com.project.InsightPrep.global.common.response.code.ApiErrorCode;
import com.project.InsightPrep.global.common.response.code.BaseErrorCode;
import java.time.format.DateTimeParseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiErrorResponse> handleCustomException(CustomException e) {
        log.error("CustomException 발생", e);

        return buildErrorResponse(e.getErrorCode());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnknownException(Exception e) {
        log.error("알 수 없는 예외 발생", e);

        return buildErrorResponse(ApiErrorCode.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleArgumentException(MethodArgumentNotValidException e) {
        return buildErrorResponse(mapFieldErrorToErrorCode(e.getFieldError()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleEnumParseError(MethodArgumentTypeMismatchException e) {
        log.error("잘못된 enum 값 또는 파라미터 타입 요청 - {}", e.getValue(), e);

        Throwable cause = e.getCause();
        while (cause != null) {
            if (cause instanceof DateTimeParseException) {
                return buildErrorResponse(ApiErrorCode.DATE_INVALID_ERROR);
            }
            cause = cause.getCause();
        }

        return buildErrorResponse(ApiErrorCode.TYPE_MISMATCH_ERROR);
    }

    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<ApiErrorResponse> handleDateParseError(DateTimeParseException e) {
        log.error("잘못된 날짜 형식 입력", e);

        return buildErrorResponse(ApiErrorCode.DATE_INVALID_ERROR);
    }

    private ResponseEntity<ApiErrorResponse> buildErrorResponse(BaseErrorCode errorCode) {
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiErrorResponse.of(errorCode));
    }

    private ResponseEntity<ApiErrorResponse> buildErrorResponse(ApiErrorResponse errorResponse) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    private ApiErrorResponse mapFieldErrorToErrorCode(FieldError error) {
        String message =  error.getDefaultMessage();

        return ApiErrorResponse.of(generateErrorCode(error), message);
    }

    private String generateErrorCode(FieldError error) {
        String field = error.getField();
        String code = error.getCode();
        return (field + "_" + code + "_" + "error").toUpperCase();
    }
}
