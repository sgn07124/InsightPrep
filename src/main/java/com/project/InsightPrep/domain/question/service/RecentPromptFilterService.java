package com.project.InsightPrep.domain.question.service;

import com.project.InsightPrep.domain.question.entity.ItemType;
import java.util.List;

public interface RecentPromptFilterService {

    void record(long memberId, String category, ItemType type, String value);

    List<String> getRecent(long memberId, String category, ItemType type, int limit);
}
