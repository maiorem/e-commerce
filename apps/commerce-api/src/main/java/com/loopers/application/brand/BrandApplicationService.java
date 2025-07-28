package com.loopers.application.brand;

import com.loopers.domain.brand.BrandModel;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BrandApplicationService {

    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;

    public List<BrandModel> getBrandList() {
        return brandRepository.findAll();
    }

    public BrandModel getBrandDetail(Long brandId) {
        return brandRepository.findById(brandId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "브랜드 정보를 찾을 수 없습니다."));
    }

    public List<ProductModel> getProductList(Long brandId) {
        return productRepository.findByBrandId(brandId);
    }
} 
