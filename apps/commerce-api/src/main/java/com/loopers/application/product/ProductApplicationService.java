package com.loopers.application.product;

import com.loopers.domain.brand.BrandModel;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.category.CategoryModel;
import com.loopers.domain.category.CategoryRepository;
import com.loopers.domain.like.ProductLikeDomainService;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductSearchDomainService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductApplicationService {

    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final ProductLikeDomainService productLikeDomainService;
    private final ProductSearchDomainService productSearchDomainService;

    /**
     * 상품 목록 조회 (페이징 / 정렬 - 최신순(기본값), 좋아요순, 가격 낮은 순, 가격 높은 순)
     */
    public List<ProductOutputInfo> getProductList(ProductQuery query) {

        productSearchDomainService.validateSearchCriteria(query.getProductName(), query.getSize());

        productSearchDomainService.validateFilterCriteria(query.getBrandId(), query.getCategoryId());
        productSearchDomainService.validateSortCriteria(query.getSortBy());

        List<ProductModel> products = productRepository.findSearchProductList(
                query.getSize(),
                query.getProductName(),
                query.getBrandId(),
                query.getCategoryId(),
                query.getSortBy(),
                query.getLastId(),
                query.getLastLikesCount(),
                query.getLastPrice(),
                query.getLastCreatedAt()
        );

        return convertToProductOutputInfoList(products);
    }

    private List<ProductOutputInfo> convertToProductOutputInfoList(List<ProductModel> productModels) {
        // 모든 브랜드 ID와 카테고리 ID를 수집
        Set<Long> brandIds = productModels.stream()
                .map(ProductModel::getBrandId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> categoryIds = productModels.stream()
                .map(ProductModel::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 모든 브랜드/카테고리 정보 조회
        Map<Long, BrandModel> brandMap = brandRepository.findAllById(brandIds).stream()
                .collect(Collectors.toMap(BrandModel::getId, brand -> brand));
        Map<Long, CategoryModel> categoryMap = categoryRepository.findAllById(categoryIds).stream()
                .collect(Collectors.toMap(CategoryModel::getId, category -> category));

        List<ProductOutputInfo> productOutputInfoList = new ArrayList<>();

        for (ProductModel model : productModels) {

            BrandModel brandModel = brandMap.get(model.getBrandId());
            CategoryModel categoryModel = categoryMap.get(model.getCategoryId());

            ProductOutputInfo outputInfo = ProductOutputInfo.convertToInfo(model, brandModel, categoryModel);
            productOutputInfoList.add(outputInfo);
        }

        return productOutputInfoList;
    }

    /**
     * 상품 상세 조회
     */
    public ProductOutputInfo getProductDetail(Long id) {

        ProductModel productModel = productRepository.findById(id)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "해당 상품을 찾을 수 없습니다."));

        // 브랜드와 카테고리 정보 조회 (null 체크 추가)
        BrandModel brandModel = null;
        if (productModel.getBrandId() != null) {
            brandModel = brandRepository.findById(productModel.getBrandId()).orElse(null);
        }

        CategoryModel categoryModel = null;
        if (productModel.getCategoryId() != null) {
            categoryModel = categoryRepository.findById(productModel.getCategoryId()).orElse(null);
        }

        return ProductOutputInfo.convertToInfo(productModel, brandModel, categoryModel);
    }
}
