package com.project.InsightPrep.domain.question.controller;

import com.project.InsightPrep.domain.question.controller.docs.QuestionControllerDocs;
import com.project.InsightPrep.domain.question.dto.request.AnswerRequest;
import com.project.InsightPrep.domain.question.dto.response.AnswerResponse.AnswerDto;
import com.project.InsightPrep.domain.question.dto.response.AnswerResponse.FeedbackDto;
import com.project.InsightPrep.domain.question.dto.response.QuestionResponse;
import com.project.InsightPrep.domain.question.dto.response.QuestionResponse.QuestionsDto;
import com.project.InsightPrep.domain.question.service.AnswerService;
import com.project.InsightPrep.domain.question.service.FeedbackService;
import com.project.InsightPrep.domain.question.service.QuestionService;
import com.project.InsightPrep.global.common.response.ApiResponse;
import com.project.InsightPrep.global.common.response.code.ApiSuccessCode;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/question")
@RequiredArgsConstructor
public class QuestionController implements QuestionControllerDocs {

    private final QuestionService questionService;
    private final AnswerService answerService;
    private final FeedbackService feedbackService;

    @Override
    @PostMapping("/{category}")
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<ApiResponse<QuestionResponse.QuestionDto>> createQuestion(@PathVariable @Valid String category) {
        QuestionResponse.QuestionDto dto = questionService.createQuestion(category);
        return ResponseEntity.ok(ApiResponse.of(ApiSuccessCode.CREATE_QUESTION_SUCCESS, dto));
    }

    @Override
    @PostMapping("/{questionId}/answer")
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<ApiResponse<AnswerDto>> saveAnswer(@RequestBody @Valid AnswerRequest.AnswerDto dto, @PathVariable Long questionId) {
        answerService.saveAnswer(dto, questionId);
        return ResponseEntity.ok(ApiResponse.of(ApiSuccessCode.SAVE_ANSWER_SUCCESS));
    }

    @Override
    @PostMapping("/{answerId}/feedback")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<FeedbackDto>> getFeedback(@PathVariable long answerId) {
        FeedbackDto feedback = feedbackService.getFeedback(answerId);
        if (feedback == null) {
            return ResponseEntity.ok(ApiResponse.of(ApiSuccessCode.FEEDBACK_PENDING));
        }
        return ResponseEntity.ok(ApiResponse.of(ApiSuccessCode.GET_FEEDBACK_SUCCESS, feedback));
    }

    @Override
    @GetMapping
    public ResponseEntity<ApiResponse<List<QuestionsDto>>> getQuestions() {
        List<QuestionsDto> dto = questionService.getQuestions();
        return ResponseEntity.ok(ApiResponse.of(ApiSuccessCode.SUCCESS, dto));
    }
}
