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

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductV1Controller implements ProductV1ApiSpec {

    private final ProductApplicationService productApplicationService;

    @Override
    @GetMapping
    public ApiResponse<ProductV1Dto.ProductListResponse> getProductList(ProductV1Dto.ProductListRequest request) {

        log.info("상품 목록 조회 요청 - productName: {}, brandId: {}, categoryId: {}, sortBy: {}, pageSize: {}, lastId: {}, lastLikesCount: {}, lastPrice: {}, lastCreatedAt: {}",
                request.productName(), request.brandId(), request.categoryId(), request.sortBy(), request.pageSize(),
                request.lastId(), request.lastLikesCount(), request.lastPrice(), request.lastCreatedAt());

        ProductQuery query = ProductQuery.from(
                request.productName(), request.brandId(), request.categoryId(), request.sortBy(),
                request.pageSize(), request.lastId(), request.lastLikesCount(), request.lastPrice(),
                request.lastCreatedAt()
        );
        

        List<ProductOutputInfo> products = productApplicationService.getProductList(query);

        ProductV1Dto.ProductListResponse response = ProductV1Dto.ProductListResponse.from(products, request.pageSize());

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
