package com.loopers.application.product;

import com.loopers.domain.product.ProductModel;
import com.loopers.support.cache.util.CacheUtil;
import com.loopers.domain.product.ProductSortBy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductCacheService {

    private final CacheUtil cacheUtil;
    
    // Redis 캐싱을 완전히 비활성화할 수 있는 플래그
    private static final boolean DISABLE_CACHE = Boolean.parseBoolean(
        System.getProperty("disable.redis.cache", "false")
    );

    private static final String PRODUCT_LIST_PREFIX = "productlist";
    private static final String PRODUCT_DETAIL_PREFIX = "productdetail";
    private static final String BRAND_PREFIX = "brand";
    private static final String CATEGORY_PREFIX = "category";

    private static final Duration PRODUCT_LIST_TTL = Duration.ofMinutes(10);
    private static final Duration PRODUCT_DETAIL_TTL = Duration.ofMinutes(30);
    private static final Duration BRAND_TTL = Duration.ofHours(2);
    private static final Duration CATEGORY_TTL = Duration.ofHours(2);

    /**
     * 첫 페이지 상품 목록 캐싱
     */
    public void cacheFirstPageProducts(ProductSortBy sortBy, List<ProductModel> products) {
        if (!isFirstPage(sortBy)) {
            return;
        }
        String cacheKey = generateFirstPageCacheKey(sortBy);
        cacheUtil.set(cacheKey, products, PRODUCT_LIST_TTL);
        log.debug("첫 페이지 상품(모델) 캐시 저장 - 정렬: {}, 상품 수: {}", sortBy, products.size());
    }

    /**
     * 첫 페이지 상품 목록 캐시 조회
     */
    public Optional<List<ProductModel>> getCachedFirstPageProducts(ProductSortBy sortBy) {
        if (!isFirstPage(sortBy)) {
            return Optional.empty();
        }
        String cacheKey = generateFirstPageCacheKey(sortBy);
        Optional<List<ProductModel>> cached = cacheUtil.getList(cacheKey, ProductModel.class);

        if (cached.isPresent()) {
            log.debug("첫 페이지 상품(모델) 캐시 히트 - 정렬: {}, 키: {}", sortBy, cacheKey);
        } else {
            log.debug("첫 페이지 상품(모델) 캐시 미스 - 정렬: {}, 키: {}", sortBy, cacheKey);
        }
        return cached;
    }

    /**
     * 첫 페이지 여부 확인
     */
    private boolean isFirstPage(ProductSortBy sortBy) {
        return sortBy == null || sortBy == ProductSortBy.LATEST || sortBy == ProductSortBy.LIKES;
    }

    /**
     * 첫 페이지 캐시 키 생성
     */
    private String generateFirstPageCacheKey(ProductSortBy sortBy) {
        if (sortBy == null || sortBy == ProductSortBy.LIKES) {
            return PRODUCT_LIST_PREFIX;
        }
        return PRODUCT_LIST_PREFIX + ":" + sortBy.name().toLowerCase();
    }

    /**
     * 상품 상세 정보 캐시 저장
     */
    public void cacheProductDetail(Long productId, ProductOutputInfo product) {
        String cacheKey = PRODUCT_DETAIL_PREFIX + ":" + productId;
        cacheUtil.set(cacheKey, product, PRODUCT_DETAIL_TTL);
        log.debug("상품상세 캐시 저장 완료 - 키: {}, 상품명: {}", cacheKey, product.name());
    }

    /**
     * 상품 상세 정보 캐시 조회
     */
    public Optional<ProductOutputInfo> getCachedProductDetail(Long productId) {
        String cacheKey = PRODUCT_DETAIL_PREFIX + ":" + productId;
        long startTime = System.currentTimeMillis();
        
        try {
            Optional<ProductOutputInfo> cached = cacheUtil.get(cacheKey, ProductOutputInfo.class);
            long cacheTime = System.currentTimeMillis() - startTime;
            
            if (cached.isPresent()) {
                log.debug("상품상세 캐시 히트 - 상품ID: {}, 캐시 조회 시간: {}ms, 키: {}", 
                        productId, cacheTime, cacheKey);
            } else {
                log.debug("상품상세 캐시 미스 - 상품ID: {}, 캐시 조회 시간: {}ms, 키: {}", 
                        productId, cacheTime, cacheKey);
            }
            
            return cached;
        } catch (Exception e) {
            long cacheTime = System.currentTimeMillis() - startTime;
            log.error("상품상세 캐시 조회 실패 - 상품ID: {}, 캐시 조회 시간: {}ms, 키: {}, 에러: {}", 
                    productId, cacheTime, cacheKey, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * 브랜드 정보 캐시 저장
     */
    public void cacheBrand(Long brandId, Object brand) {
        String cacheKey = BRAND_PREFIX + ":" + brandId;
        cacheUtil.set(cacheKey, brand, BRAND_TTL);
        log.debug("브랜드 캐시 저장 완료 - 키: {}", cacheKey);
    }

    /**
     * 브랜드 정보 캐시 조회
     */
    public <T> Optional<T> getCachedBrand(Long brandId, Class<T> clazz) {
        String cacheKey = BRAND_PREFIX + ":" + brandId;
        Optional<T> cached = cacheUtil.get(cacheKey, clazz);
        
        if (cached.isPresent()) {
            log.debug("브랜드 캐시 히트 - 키: {}", cacheKey);
        } else {
            log.debug("브랜드 캐시 미스 - 키: {}", cacheKey);
        }
        
        return cached;
    }

    /**
     * 카테고리 정보 캐시 저장
     */
    public void cacheCategory(Long categoryId, Object category) {
        String cacheKey = CATEGORY_PREFIX + ":" + categoryId;
        cacheUtil.set(cacheKey, category, CATEGORY_TTL);
        log.debug("카테고리 캐시 저장 완료 - 키: {}", cacheKey);
    }

    /**
     * 카테고리 정보 캐시 조회
     */
    public <T> Optional<T> getCachedCategory(Long categoryId, Class<T> clazz) {
        String cacheKey = CATEGORY_PREFIX + ":" + categoryId;
        Optional<T> cached = cacheUtil.get(cacheKey, clazz);
        
        if (cached.isPresent()) {
            log.debug("카테고리 캐시 히트 - 키: {}", cacheKey);
        } else {
            log.debug("카테고리 캐시 미스 - 키: {}", cacheKey);
        }
        
        return cached;
    }

    /**
     * 상품 관련 캐시 무효화
     * 상품 정보가 변경될 때 호출
     */
    public void evictProductCache() {
        cacheUtil.deleteByPattern(PRODUCT_LIST_PREFIX + "*");
        cacheUtil.deleteByPattern(PRODUCT_DETAIL_PREFIX + "*");
        log.info("상품 관련 캐시 무효화 완료");
    }

    /**
     * 첫 페이지 캐시만 무효화
     */
    public void evictFirstPageCache() {
        cacheUtil.deleteByPattern(PRODUCT_LIST_PREFIX + ":sort:*:first");
        log.info("첫 페이지 캐시 무효화 완료");
    }

    /**
     * 특정 상품 캐시 무효화
     */
    public void evictProductDetailCache(Long productId) {
        String cacheKey = PRODUCT_DETAIL_PREFIX + ":" + productId;
        cacheUtil.delete(cacheKey);
        log.info("상품 상세 캐시 무효화 - 상품ID: {}", productId);
    }

    /**
     * 브랜드 관련 캐시 무효화
     */
    public void evictBrandCache() {
        cacheUtil.deleteByPattern(BRAND_PREFIX + "*");
        cacheUtil.deleteByPattern(PRODUCT_LIST_PREFIX + "*");
        log.info("브랜드 관련 캐시 무효화 완료");
    }

    /**
     * 카테고리 관련 캐시 무효화
     */
    public void evictCategoryCache() {
        cacheUtil.deleteByPattern(CATEGORY_PREFIX + "*");
        cacheUtil.deleteByPattern(PRODUCT_LIST_PREFIX + "*");
        log.info("카테고리 관련 캐시 무효화 완료");
    }
}
