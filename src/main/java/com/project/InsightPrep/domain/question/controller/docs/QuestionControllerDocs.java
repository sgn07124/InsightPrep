package com.project.InsightPrep.domain.question.controller.docs;

import com.project.InsightPrep.domain.question.dto.request.AnswerRequest;
import com.project.InsightPrep.domain.question.dto.response.AnswerResponse;
import com.project.InsightPrep.domain.question.dto.response.AnswerResponse.AnswerDto;
import com.project.InsightPrep.domain.question.dto.response.PageResponse;
import com.project.InsightPrep.domain.question.dto.response.PreviewResponse;
import com.project.InsightPrep.domain.question.dto.response.QuestionResponse;
import com.project.InsightPrep.domain.question.dto.response.QuestionResponse.QuestionsDto;
import com.project.InsightPrep.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Question", description = "Question 관련 API")
public interface QuestionControllerDocs {

    @Operation(summary = "질문 생성", description = "로그인한 사용자는 카테고리를 선택한 후 질문 생성을 합니다. 생성된 질문은 답변을 할 시 db에 저장이 되며 이후에 조회가 가능합니다.")
    public ResponseEntity<ApiResponse<QuestionResponse.QuestionDto>> createQuestion(@PathVariable String category);

    @Operation(summary = "답변 작성", description = "질문에 대한 답변을 작성합니다.")
    public ResponseEntity<ApiResponse<AnswerDto>> saveAnswer(@RequestBody @Valid AnswerRequest.AnswerDto dto, @PathVariable Long questionId);

    @Operation(summary = "피드백 조회", description = "작성한 답변에 대한 피드백을 조회합니다. 폴링 방식을 적용하여 프론트엔드에서 일정 시간(3초)마다 해당 요청을 하고, 피드백이 생성되었으면 반환, 아니면 202(PENDING)으로 반복하도록 구성합니다.")
    public ResponseEntity<ApiResponse<AnswerResponse.FeedbackDto>> getFeedback(@PathVariable long answerId);

    @Operation(summary = "면접 질문들 조회", description = "본인이 답변한 질문들을 리스트로 조회합니다.")
    public ResponseEntity<ApiResponse<PageResponse<QuestionsDto>>> getQuestions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    );

    @Operation(summary = "특정 면접 질문 삭제", description = "본인이 답변한 질문들을 리스트로 조회했을 때, 원하는 질문에 대하여 삭제합니다. 해당 질문 삭제 시, 질문에 대한 답변과 피드백 모두 삭제됩니다. "
            + "답변 id로 삭제가 진행되며 피드백이 연쇄 삭제되고, 질문은 상태가 WAITING으로 수정되어 자동으로 삭제됩니다.")
    public ResponseEntity<ApiResponse<?>> deleteQuestion(@PathVariable long answerId);

    @Operation(summary = "답변에 대한 프리뷰 조회", description = "연결된 질문과 답변 조회 시 사용합니다.")
    public ResponseEntity<ApiResponse<PreviewResponse>> getPreview(@PathVariable long answerId);
}
