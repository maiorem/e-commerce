package com.loopers.application.brand;

import com.loopers.domain.brand.BrandModel;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BrandApplicationService {

    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;

    /**
     * 브랜드 목록 조회 (캐싱 적용)
     */
    @Cacheable(value = "brand", key = "'list'")
    public List<BrandModel> getBrandList() {
        log.info("브랜드 목록 DB 조회 (캐시 미스)");
        return brandRepository.findAll();
    }

    /**
     * 브랜드 상세 조회 (캐싱 적용)
     */
    @Cacheable(value = "brand", key = "#brandId")
    public BrandModel getBrandDetail(Long brandId) {
        log.info("브랜드 상세 DB 조회 (캐시 미스) - 브랜드ID: {}", brandId);
        return brandRepository.findById(brandId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "브랜드 정보를 찾을 수 없습니다."));
    }

    /**
     * 브랜드별 상품 목록 조회
     */
    public List<ProductModel> getProductList(Long brandId) {
        return productRepository.findByBrandId(brandId);
    }
} 
