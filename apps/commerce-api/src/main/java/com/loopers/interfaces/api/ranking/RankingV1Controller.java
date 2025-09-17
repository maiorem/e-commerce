package com.loopers.interfaces.api.ranking;

import com.loopers.application.ranking.RankingApplicationService;
import com.loopers.application.ranking.RankingPageInfo;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/v1/rankings")
@RequiredArgsConstructor
public class RankingV1Controller implements RankingV1ApiSpec {

    private final RankingApplicationService rankingApplicationService;

    @GetMapping
    public ApiResponse<RankingV1Dto.RankingPageResponse> getRankings(
            @RequestParam(name = "date") @DateTimeFormat(pattern = "yyyyMMdd") LocalDate date,
            @RequestParam(name = "period", defaultValue = "daily") String period,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "page", defaultValue = "1") int page) {
        
        log.info("랭킹 페이지 조회 요청 - Date: {}, Period: {}, Page: {}, Size: {}", date, period, page, size);
        
        // 페이지 유효성 검증
        if (page < 1) {
            page = 1;
        }
        if (size < 1 || size > 100) {
            size = 20;
        }
        
        RankingPageInfo rankingPageInfo = rankingApplicationService.getRankingPage(date, period, page, size);
        RankingV1Dto.RankingPageResponse response = RankingV1Dto.RankingPageResponse.from(rankingPageInfo);
        
        return ApiResponse.success(response);
    }
}