package com.loopers.interfaces.api.ranking;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Tag(name = "Ranking API", description = "상품 랭킹 조회 API")
public interface RankingV1ApiSpec {

    @Operation(
            summary = "일간 상품 랭킹 조회",
            description = "특정 날짜의 상품 랭킹을 페이지네이션으로 조회합니다."
    )
    ApiResponse<RankingV1Dto.RankingPageResponse> getRankings(
            @Parameter(description = "조회할 날짜 (yyyyMMdd 형식)", required = true, example = "20231215")
            @RequestParam(name = "date") @DateTimeFormat(pattern = "yyyyMMdd") LocalDate date,
            
            @Parameter(description = "페이지당 항목 수 (1-100, 기본값: 20)", example = "20")
            @RequestParam(name = "size", defaultValue = "20") int size,
            
            @Parameter(description = "페이지 번호 (1부터 시작, 기본값: 1)", example = "1")
            @RequestParam(name = "page", defaultValue = "1") int page
    );
}