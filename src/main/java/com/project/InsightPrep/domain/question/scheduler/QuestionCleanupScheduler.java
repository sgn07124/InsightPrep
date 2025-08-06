package com.project.InsightPrep.domain.question.scheduler;

import com.project.InsightPrep.domain.question.entity.AnswerStatus;
import com.project.InsightPrep.domain.question.mapper.QuestionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuestionCleanupScheduler {

    private final QuestionMapper questionMapper;

    @Scheduled(cron = "0 0 3 * * *")  // 매일 새벽 3시에 실행
    public void deleteOldUnansweredQuestions() {
        questionMapper.deleteUnansweredQuestions(AnswerStatus.WAITING.name());
    }
}
