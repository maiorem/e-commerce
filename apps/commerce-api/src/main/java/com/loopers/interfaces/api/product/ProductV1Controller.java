package com.loopers.interfaces.api.product;

import com.loopers.application.product.ProductApplicationService;
import com.loopers.application.product.ProductOutputInfo;
import com.loopers.application.product.ProductQuery;
import com.loopers.application.ranking.RankingApplicationService;
import com.loopers.application.ranking.RankingInfo;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductV1Controller implements ProductV1ApiSpec {

    private final ProductApplicationService productApplicationService;
    private final RankingApplicationService rankingApplicationService;

    @Override
    @GetMapping
    public ApiResponse<ProductV1Dto.ProductListResponse> getProductList(
            @RequestHeader(value = "X-USER-ID", required = false) String userId,
            ProductV1Dto.ProductListRequest request) {

        log.info("상품 목록 조회 요청 - productName: {}, brandId: {}, categoryId: {}, sortBy: {}, pageSize: {}, lastId: {}, lastLikesCount: {}, lastPrice: {}, lastCreatedAt: {}",
                request.productName(), request.brandId(), request.categoryId(), request.sortBy(), request.pageSize(),
                request.lastId(), request.lastLikesCount(), request.lastPrice(), request.lastCreatedAt());


        ProductQuery query = ProductQuery.from(
                request.productName(), request.brandId(), request.categoryId(), request.sortBy(),
                request.pageSize(), request.lastId(), request.lastLikesCount(), request.lastPrice(), request.lastCreatedAt()
        );
        
        List<ProductOutputInfo> products = productApplicationService.getProductList(query);

        ProductV1Dto.ProductListResponse responseBody = ProductV1Dto.ProductListResponse.from(products, request.pageSize());

        return ApiResponse.success(responseBody);
    }

    @Override
    @GetMapping("/{productId}")
    public ApiResponse<ProductV1Dto.ProductResponseDto> getProductDetail(
            @RequestHeader(value = "X-USER-ID", required = false) String userId,
            Long productId) {

        ProductOutputInfo product = productApplicationService.getProductDetail(productId, userId);
        
        // 오늘 날짜 기준 랭킹 정보 조회
        RankingInfo rankingInfo = rankingApplicationService.getProductRankingInfo(productId, LocalDate.now());
        ProductV1Dto.RankingInfoDto rankingDto = null;
        if (rankingInfo.getRank() != null) {
            rankingDto = ProductV1Dto.RankingInfoDto.from(rankingInfo.getRank(), rankingInfo.getScore());
        }
        
        ProductV1Dto.ProductResponseDto responseBody = ProductV1Dto.ProductResponseDto.from(
                product.id(), product.name(), product.brandName(), product.categoryName(),
                product.price(), product.likeCount(), product.stock(), rankingDto
        );

        return ApiResponse.success(responseBody);
    }
}
