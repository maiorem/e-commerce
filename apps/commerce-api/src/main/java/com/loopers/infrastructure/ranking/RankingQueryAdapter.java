package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.RankingCacheProcessor;
import com.loopers.domain.ranking.RankingItem;
import com.loopers.domain.ranking.RankingPage;
import com.loopers.domain.ranking.RankingQueryRepository;
import com.loopers.infrastructure.product.ProductJpaRepository;
import com.loopers.domain.product.ProductModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class RankingQueryAdapter implements RankingQueryRepository {

    private final RankingCacheProcessor rankingCacheProcessor;
    private final ProductJpaRepository productRepository;

    @Override
    public RankingPage getRankingWithProducts(LocalDate date, int page, int size) {
        log.debug("랭킹과 상품 정보 통합 조회 시작 - Date: {}, Page: {}, Size: {}", date, page, size);
        
        // 1. Redis에서 랭킹 조회
        Set<Object> rankingProductIds = rankingCacheProcessor.getRankingByPage(date, page, size);
        
        if (rankingProductIds.isEmpty()) {
            log.debug("랭킹 데이터가 없습니다 - Date: {}", date);
            return RankingPage.empty(page, size);
        }
        
        // 2. 상품 ID를 Long으로 변환
        List<Long> productIds = rankingProductIds.stream()
                .map(id -> Long.valueOf(id.toString()))
                .toList();
        
        // 3. DB에서 상품 정보 일괄 조회
        List<ProductModel> products = productRepository.findAllById(productIds);
        Map<Long, ProductModel> productMap = products.stream()
                .collect(Collectors.toMap(ProductModel::getId, Function.identity()));
        
        // 4. 랭킹 아이템 정보 구성
        List<RankingItem> rankingItems = productIds.stream()
                .map(productId -> {
                    Long rank = rankingCacheProcessor.getProductRank(productId, date);
                    Double score = rankingCacheProcessor.getProductScore(productId, date);
                    ProductModel product = productMap.get(productId);
                    
                    RankingItem.RankingItemBuilder builder = RankingItem.builder()
                            .productId(productId)
                            .rank(rank)
                            .score(score);
                    
                    if (product != null) {
                        RankingItem.ProductInfo productInfo = RankingItem.ProductInfo.builder()
                                .name(product.getName())
                                .description(product.getDescription())
                                .price(product.getPrice())
                                .brandName("Brand " + product.getBrandId()) // 임시로 브랜드 ID 사용
                                .likeCount(product.getLikesCount())
                                .build();
                        
                        builder = builder.productInfo(productInfo);
                    }
                    
                    return builder.build();
                })
                .toList();

        // 동일 점수 상품들의 정렬 기준 추가
        List<RankingItem> sortedRankingItems = rankingItems.stream()
                .sorted((a, b) -> {
                    int scoreCompare = Double.compare(b.getScore(), a.getScore());
                    if (scoreCompare == 0) {
                        // 점수가 같으면 상품 ID 오름차순 (등록일 대용)
                        return Long.compare(a.getProductId(), b.getProductId());
                    }
                    return scoreCompare;
                })
                .toList();
        
        // 5. 전체 랭킹 수 조회
        Long totalCount = rankingCacheProcessor.getTotalRankingCount(date);
        
        log.debug("랭킹과 상품 정보 통합 조회 완료 - Date: {}, 조회된 아이템 수: {}, 전체 수: {}", 
                 date, rankingItems.size(), totalCount);
        
        return RankingPage.builder()
                .items(sortedRankingItems)
                .currentPage(page)
                .pageSize(size)
                .totalCount(totalCount)
                .totalPages((int) Math.ceil((double) totalCount / size))
                .hasNext((long) page * size < totalCount)
                .build();
    }
}
