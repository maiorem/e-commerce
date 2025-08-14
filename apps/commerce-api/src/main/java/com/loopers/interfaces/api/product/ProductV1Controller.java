package com.loopers.interfaces.api.product;

import com.loopers.application.product.ProductApplicationService;
import com.loopers.application.product.ProductOutputInfo;
import com.loopers.application.product.ProductQuery;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductV1Controller implements ProductV1ApiSpec {

    private final ProductApplicationService productApplicationService;

    @Override
    @GetMapping
    public ApiResponse<ProductV1Dto.ProductListResponse> getProductList(ProductV1Dto.ProductListRequest request, HttpServletResponse response) {

        log.info("상품 목록 조회 요청 - productName: {}, brandId: {}, categoryId: {}, sortBy: {}, pageSize: {}, lastId: {}, lastLikesCount: {}, lastPrice: {}, lastCreatedAt: {}",
                request.productName(), request.brandId(), request.categoryId(), request.sortBy(), request.pageSize(),
                request.lastId(), request.lastLikesCount(), request.lastPrice(), request.lastCreatedAt());

        long startTime = System.currentTimeMillis();

        ProductQuery query = ProductQuery.from(
                request.productName(), request.brandId(), request.categoryId(), request.sortBy(),
                request.pageSize(), request.lastId(), request.lastLikesCount(), request.lastPrice(), request.lastCreatedAt()
        );
        
        List<ProductOutputInfo> products = productApplicationService.getProductList(query);

        ProductV1Dto.ProductListResponse responseBody = ProductV1Dto.ProductListResponse.from(products, request.pageSize());

        // 응답 시간 측정 및 헤더 추가
        long totalTime = System.currentTimeMillis() - startTime;
        response.addHeader("X-Response-Time", String.valueOf(totalTime) + "ms");
        
        // 첫 페이지 여부 확인
        boolean isFirstPage = request.lastId() == null && 
                             request.lastLikesCount() == null && 
                             request.lastPrice() == null && 
                             request.lastCreatedAt() == null;
        
        if (isFirstPage) {
            response.addHeader("X-Page-Type", "first");
            response.addHeader("X-Cache-Strategy", "first-page-only");
        } else {
            response.addHeader("X-Page-Type", "paging");
            response.addHeader("X-Cache-Strategy", "no-cache");
        }

        return ApiResponse.success(responseBody);
    }

    @Override
    @GetMapping("/{productId}")
    public ApiResponse<ProductV1Dto.ProductResponseDto> getProductDetail(
            Long productId, HttpServletResponse response) {

        long startTime = System.currentTimeMillis();

        ProductOutputInfo product = productApplicationService.getProductDetail(productId);
        ProductV1Dto.ProductResponseDto responseBody = ProductV1Dto.ProductResponseDto.from(
                product.id(), product.name(), product.brandName(), product.categoryName(),
                product.price(), product.likeCount(), product.stock()
        );

        // 응답 시간 측정 및 헤더 추가
        long totalTime = System.currentTimeMillis() - startTime;
        response.addHeader("X-Response-Time", String.valueOf(totalTime) + "ms");
        response.addHeader("X-Cache-Strategy", "product-detail");

        return ApiResponse.success(responseBody);
    }
}
