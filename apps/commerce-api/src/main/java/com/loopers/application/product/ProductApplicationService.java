package com.loopers.application.product;

import com.loopers.domain.brand.BrandModel;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.category.CategoryModel;
import com.loopers.domain.category.CategoryRepository;
import com.loopers.domain.like.LikeRepository;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductApplicationService {

    private final ProductRepository productRepository;
    private final LikeRepository likeRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;

    /**
     * 상품 목록 조회 (페이징 / 정렬 - 최신순(기본값), 좋아요순, 가격 낮은 순, 가격 높은 순)
     */
    public Page<ProductOutputInfo> getProductList(Pageable pageable, ProductQuery query) {
        // 브랜드와 카테고리 이름으로 ID를 조회
        BrandModel brand = brandRepository.findByName(query.getBrandName()).orElse(null);
        CategoryModel category = categoryRepository.findByName(query.getCategoryName()).orElse(null);

        Long brandId = (brand != null) ? brand.getId() : null;
        Long categoryId = (category != null) ? category.getId() : null;

        // 상품 목록 조회
        Page<ProductModel> productPage = productRepository.findSearchProductList(
                pageable,
                query.getProductName(),
                brandId,
                categoryId,
                query.getSortBy()
        );

        // 상품 목록을 ProductOutputInfo로 변환
        return convertToProductOutputInfoPage(productPage);
    }

    public Page<ProductOutputInfo> convertToProductOutputInfoPage(Page<ProductModel> productPage) {
        List<ProductOutputInfo> productOutputInfoList = new ArrayList<>();

        // 각 상품에 대해 좋아요 수, 브랜드, 카테고리 정보를 조회하여 ProductOutputInfo로 변환
        for (ProductModel model : productPage) {
            // 좋아요 수 조회
            int likeCount = likeRepository.countByProductId(model.getId());

            // 브랜드와 카테고리 정보 조회 (null 체크 추가)
            BrandModel brandModel = null;
            if (model.getBrandId() != null) {
                brandModel = brandRepository.findById(model.getBrandId()).orElse(null);
            }
            
            CategoryModel categoryModel = null;
            if (model.getCategoryId() != null) {
                categoryModel = categoryRepository.findById(model.getCategoryId()).orElse(null);
            }

            ProductOutputInfo outputInfo = ProductOutputInfo.convertToInfo(model, brandModel, categoryModel, likeCount);
            productOutputInfoList.add(outputInfo);
        }

        return new PageImpl<>(productOutputInfoList, productPage.getPageable(), productPage.getTotalElements());
    }

    /**
     * 상품 상세 조회
     */
    public ProductOutputInfo getProductDetail(Long id) {

        ProductModel productModel = productRepository.findById(id)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "해당 상품을 찾을 수 없습니다."));

        int likeCount = likeRepository.countByProductId(productModel.getId());

        // 브랜드와 카테고리 정보 조회 (null 체크 추가)
        BrandModel brandModel = null;
        if (productModel.getBrandId() != null) {
            brandModel = brandRepository.findById(productModel.getBrandId()).orElse(null);
        }
        
        CategoryModel categoryModel = null;
        if (productModel.getCategoryId() != null) {
            categoryModel = categoryRepository.findById(productModel.getCategoryId()).orElse(null);
        }

        return ProductOutputInfo.convertToInfo(productModel, brandModel, categoryModel, likeCount);
    }
}
