package com.loopers.interfaces.api.product;

import com.loopers.application.product.ProductApplicationService;
import com.loopers.application.product.ProductOutputInfo;
import com.loopers.application.product.ProductQuery;
import com.loopers.domain.product.ProductSortBy;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductV1Controller implements ProductV1ApiSpec {

    private final ProductApplicationService productApplicationService;

    @Override
    @GetMapping
    public ApiResponse<ProductV1Dto.ProductListResponse> getProductList(
            String productName,
            String brandName,
            String categoryName,
            ProductSortBy sortBy,
            int pageNumber,
            int pageSize
    ) {
        log.info("상품 목록 조회 요청 - productName: {}, brandName: {}, categoryName: {}, sortBy: {}, page: {}, size: {}", 
                productName, brandName, categoryName, sortBy, pageNumber, pageSize);
        
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        ProductQuery query = ProductQuery.from(productName, brandName, categoryName, sortBy, pageNumber, pageSize);
        Page<ProductOutputInfo> products = productApplicationService.getProductList(pageable, query);
        
        ProductV1Dto.ProductListResponse response = ProductV1Dto.ProductListResponse.from(products);
        log.info("상품 목록 조회 완료 - 총 {}건, 현재 페이지 {}건", response.totalCount(), response.products().size());
        
        return ApiResponse.success(response);
    }

    @Override
    @GetMapping("/{productId}")
    public ApiResponse<ProductV1Dto.ProductResponseDto> getProductDetail(
            Long productId
    ) {
        log.info("상품 상세 조회 요청 - productId: {}", productId);
        
        ProductOutputInfo product = productApplicationService.getProductDetail(productId);
        ProductV1Dto.ProductResponseDto response = ProductV1Dto.ProductResponseDto.from(
                product.id(), product.name(), product.brandName(), product.categoryName(),
                product.price(), product.likeCount(), product.stock()
        );
        
        log.info("상품 상세 조회 완료 - productId: {}, name: {}", productId, product.name());
        return ApiResponse.success(response);
    }
}
