package com.loopers.interfaces.api.product;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

import jakarta.servlet.http.HttpServletResponse;

@Tag(name = "Product V1 API", description = "상품 조회 API")
public interface ProductV1ApiSpec {

    @GetMapping
    @Operation(summary = "상품 목록 조회", description = "브랜드, 카테고리 필터링 및 정렬 기능을 제공합니다.")
    ApiResponse<ProductV1Dto.ProductListResponse> getProductList(
            @ModelAttribute ProductV1Dto.ProductListRequest productListRequest,
            HttpServletResponse response
    );

    @GetMapping("/{productId}")
    @Operation(summary = "상품 상세 조회", description = "상품 ID로 상품 상세 정보를 조회합니다.")
    ApiResponse<ProductV1Dto.ProductResponseDto> getProductDetail(
            @Parameter(description = "상품 ID") @PathVariable(value = "productId") Long productId,
            HttpServletResponse response
    );
}
