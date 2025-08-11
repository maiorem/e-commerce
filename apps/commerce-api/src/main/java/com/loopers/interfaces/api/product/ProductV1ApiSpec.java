package com.loopers.interfaces.api.product;

import com.loopers.interfaces.api.ApiResponse;
import com.loopers.domain.product.ProductSortBy;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Product V1 API", description = "상품 조회 API")
public interface ProductV1ApiSpec {

    @GetMapping
    @Operation(summary = "상품 목록 조회", description = "브랜드, 카테고리 필터링 및 정렬 기능을 제공합니다.")
    ApiResponse<ProductV1Dto.ProductListResponse> getProductList(
            @Parameter(description = "상품명 검색") @RequestParam(required = false) String productName,
            @Parameter(description = "브랜드명") @RequestParam(required = false) String brandName,
            @Parameter(description = "카테고리명") @RequestParam(required = false) String categoryName,
            @Parameter(description = "정렬 기준") @RequestParam(defaultValue = "LATEST") ProductSortBy sortBy,
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int pageNumber,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int pageSize
    );

    @GetMapping("/{productId}")
    @Operation(summary = "상품 상세 조회", description = "상품 ID로 상품 상세 정보를 조회합니다.")
    ApiResponse<ProductV1Dto.ProductResponseDto> getProductDetail(
            @Parameter(description = "상품 ID") @PathVariable(value = "productId") Long productId
    );
}
