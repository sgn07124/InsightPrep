package com.project.InsightPrep.domain.question.scheduler;

import com.project.InsightPrep.domain.question.entity.AnswerStatus;
import com.project.InsightPrep.domain.question.mapper.QuestionMapper;
import com.project.InsightPrep.domain.question.repository.QuestionRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class QuestionCleanupScheduler {

    private final QuestionRepository questionRepository;

    @Scheduled(cron = "0 0 3 * * *")  // 매일 새벽 3시에 실행
    public void deleteOldUnansweredQuestions() {
        questionRepository.deleteByStatusAndCreatedAtBefore(AnswerStatus.WAITING, LocalDateTime.now().minusHours(1));
        log.info("1시간 이상 지난 미답변 질문 삭제 완료");
    }
}
