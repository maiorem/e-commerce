package com.loopers.application.product;

import com.loopers.domain.brand.BrandModel;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.category.CategoryModel;
import com.loopers.domain.category.CategoryRepository;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductSearchDomainService;
import com.loopers.domain.product.ProductSortBy;
import com.loopers.domain.product.event.ProductDetailViewedPublisher;
import com.loopers.domain.product.event.ProductViewedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductApplicationService {

    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final ProductSearchDomainService productSearchDomainService;
    private final ProductCacheService productCacheService;

    private final ProductDetailViewedPublisher detailViewedPublisher;

    /**
     * 상품 목록 조회 (페이징 / 정렬 - 최신순(기본값), 좋아요순, 가격 낮은 순, 가격 높은 순)
     */
    public List<ProductOutputInfo> getProductList(ProductQuery query) {

        productSearchDomainService.validateSearchCriteria(query.getProductName(), query.getSize());
        productSearchDomainService.validateFilterCriteria(query.getBrandId(), query.getCategoryId());
        productSearchDomainService.validateSortCriteria(query.getSortBy());

        List<ProductModel> products;

        // 1. 첫 페이지 요청일 경우 캐시 조회 시도
        if (isFirstPage(query)) {
            Optional<List<ProductModel>> cachedProducts = productCacheService.getCachedFirstPageProducts(query.getSortBy());
            if (cachedProducts.isPresent()) {
                log.debug("첫 페이지 캐시 히트");
                products = cachedProducts.get();
            } else {
                // 2. 캐시 미스 시 DB 조회 및 캐시 저장
                log.debug("첫 페이지 캐시 미스");
                products = productRepository.findSearchProductList(
                    query.getSize(), query.getProductName(), query.getBrandId(), query.getCategoryId(),
                    query.getSortBy(), query.getLastId(), query.getLastLikesCount(),
                    query.getLastPrice(), query.getLastCreatedAt()
                );
                productCacheService.cacheFirstPageProducts(query.getSortBy(), products);
            }
        } else {
            // 3. 첫 페이지가 아닐 경우 DB에서 바로 조회
            products = productRepository.findSearchProductList(
                query.getSize(), query.getProductName(), query.getBrandId(), query.getCategoryId(),
                query.getSortBy(), query.getLastId(), query.getLastLikesCount(),
                query.getLastPrice(), query.getLastCreatedAt()
            );
        }

        return convertToProductOutputInfoList(products);
    }

    /**
     * 첫 페이지 확인
     */
    private boolean isFirstPage(ProductQuery query) {
        return query.getLastId() == null &&
               (query.getSortBy() == null || query.getSortBy() == ProductSortBy.LATEST || query.getSortBy() == ProductSortBy.LIKES);
    }

    private List<ProductOutputInfo> convertToProductOutputInfoList(List<ProductModel> productModels) {
        if (productModels == null || productModels.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Long> brandIds = productModels.stream().map(ProductModel::getBrandId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> categoryIds = productModels.stream().map(ProductModel::getCategoryId).filter(Objects::nonNull).collect(Collectors.toSet());

        Map<Long, BrandModel> brandMap = getBrandsFromCacheOrDb(brandIds);
        Map<Long, CategoryModel> categoryMap = getCategoriesFromCacheOrDb(categoryIds);

        List<ProductOutputInfo> productOutputInfoList = new ArrayList<>();
        for (ProductModel model : productModels) {
            BrandModel brandModel = brandMap.get(model.getBrandId());
            CategoryModel categoryModel = categoryMap.get(model.getCategoryId());
            ProductOutputInfo outputInfo = ProductOutputInfo.convertToInfo(model, brandModel, categoryModel);
            productOutputInfoList.add(outputInfo);
        }
        return productOutputInfoList;
    }

    private Map<Long, BrandModel> getBrandsFromCacheOrDb(Set<Long> brandIds) {
        if (brandIds == null || brandIds.isEmpty()) return Collections.emptyMap();
        Map<Long, BrandModel> brandMap = new HashMap<>();
        Set<Long> missingBrandIds = new HashSet<>();

        for (Long brandId : brandIds) {
            productCacheService.getCachedBrand(brandId, BrandModel.class)
                .ifPresentOrElse(brand -> brandMap.put(brandId, brand), () -> missingBrandIds.add(brandId));
        }

        if (!missingBrandIds.isEmpty()) {
            List<BrandModel> newBrands = brandRepository.findAllById(missingBrandIds);
            for (BrandModel brand : newBrands) {
                brandMap.put(brand.getId(), brand);
                productCacheService.cacheBrand(brand.getId(), brand);
            }
        }
        return brandMap;
    }

    private Map<Long, CategoryModel> getCategoriesFromCacheOrDb(Set<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) return Collections.emptyMap();
        Map<Long, CategoryModel> categoryMap = new HashMap<>();
        Set<Long> missingCategoryIds = new HashSet<>();

        for (Long categoryId : categoryIds) {
            productCacheService.getCachedCategory(categoryId, CategoryModel.class)
                .ifPresentOrElse(category -> categoryMap.put(categoryId, category), () -> missingCategoryIds.add(categoryId));
        }

        if (!missingCategoryIds.isEmpty()) {
            List<CategoryModel> newCategories = categoryRepository.findAllById(missingCategoryIds);
            for (CategoryModel category : newCategories) {
                categoryMap.put(category.getId(), category);
                productCacheService.cacheCategory(category.getId(), category);
            }
        }
        return categoryMap;
    }

    /**
     * 상품 상세 조회
     */
    public ProductOutputInfo getProductDetail(Long id, String userId) {

        ProductModel productModel = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 상품을 찾을 수 없습니다."));

        // 브랜드와 카테고리 정보 조회
        BrandModel brandModel = null;
        if (productModel.getBrandId() != null) {
            brandModel = brandRepository.findById(productModel.getBrandId()).orElse(null);
        }
        
        CategoryModel categoryModel = null;
        if (productModel.getCategoryId() != null) {
            categoryModel = categoryRepository.findById(productModel.getCategoryId()).orElse(null);
        }

        ProductOutputInfo result = ProductOutputInfo.convertToInfo(productModel, brandModel, categoryModel);

        if (userId != null && !userId.isBlank()) {
            try {
                // 상품 조회 이벤트 발행
                detailViewedPublisher.publish(
                        ProductViewedEvent.createDetailView(id, userId)
                );
            } catch (Exception e) {
                log.warn("상품 조회 이벤트 발행 실패 - ProductId: {}, UserId: {}", id, userId, e);
            }
        }
        return result;
    }
}
