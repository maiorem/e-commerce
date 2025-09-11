package com.loopers.application.ranking;

import com.loopers.domain.ranking.RankingCacheProcessor;
import com.loopers.domain.ranking.RankingPage;
import com.loopers.domain.ranking.RankingQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankingApplicationService {

    private final RankingCacheProcessor rankingCacheProcessor;
    private final RankingQueryRepository rankingQueryRepository;

    public RankingPageInfo getRankingPage(LocalDate date, int page, int size) {
        log.debug("랭킹 페이지 조회 - Date: {}, Page: {}, Size: {}", date, page, size);
        
        RankingPage domainRankingPage = rankingQueryRepository.getRankingWithProducts(date, page, size);
        
        List<RankingItemInfo> applicationItems = domainRankingPage.getItems().stream()
                .map(domainItem -> {
                    RankingItemInfo.RankingItemInfoBuilder builder = RankingItemInfo.builder()
                            .productId(domainItem.getProductId())
                            .rank(domainItem.getRank())
                            .score(domainItem.getScore());
                    
                    if (domainItem.getProductInfo() != null) {
                        RankingItemInfo.ProductInfo applicationProductInfo = RankingItemInfo.ProductInfo.builder()
                                .name(domainItem.getProductInfo().getName())
                                .description(domainItem.getProductInfo().getDescription())
                                .price(domainItem.getProductInfo().getPrice())
                                .brandName(domainItem.getProductInfo().getBrandName())
                                .likeCount(domainItem.getProductInfo().getLikeCount())
                                .build();
                        
                        return builder.build().withProductInfo(applicationProductInfo);
                    }
                    
                    return builder.build();
                })
                .toList();
        
        return RankingPageInfo.builder()
                .items(applicationItems)
                .page(domainRankingPage.getCurrentPage())
                .size(domainRankingPage.getPageSize())
                .totalItems(domainRankingPage.getTotalCount().intValue())
                .date(date)
                .build();
    }

    public RankingInfo getProductRankingInfo(Long productId, LocalDate date) {
        Long rank = rankingCacheProcessor.getProductRank(productId, date);
        Double score = rankingCacheProcessor.getProductScore(productId, date);
        
        return RankingInfo.builder()
                .productId(productId)
                .date(date)
                .rank(rank)
                .score(score)
                .build();
    }
}
