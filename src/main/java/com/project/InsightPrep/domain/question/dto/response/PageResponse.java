package com.project.InsightPrep.domain.question.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
public class PageResponse<T> {

    private List<T> content;
    private int page;
    private int size;
    private long totalElements;

    public long getTotalPages() {
        return (totalElements + size - 1) / size;
    }

    public boolean isFirst() { return page <= 1; }
    public boolean isLast()  { return page >= getTotalPages(); }
}
