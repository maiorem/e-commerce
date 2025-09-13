package com.loopers.domain.ranking;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RankingPage {
    private final List<RankingItem> items;
    private final int currentPage;
    private final int pageSize;
    private final Long totalCount;
    private final int totalPages;
    private final boolean hasNext;
    
    public static RankingPage empty(int page, int size) {
        return RankingPage.builder()
                .items(List.of())
                .currentPage(page)
                .pageSize(size)
                .totalCount(0L)
                .totalPages(0)
                .hasNext(false)
                .build();
    }
}