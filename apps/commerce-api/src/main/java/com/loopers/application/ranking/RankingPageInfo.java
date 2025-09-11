package com.loopers.application.ranking;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class RankingPageInfo {
    private final LocalDate date;
    private final int page;
    private final int size;
    private final int totalItems;
    private final List<RankingItemInfo> items;
}