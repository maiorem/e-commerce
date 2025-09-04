package com.loopers.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CacheInvalidationDomainService {

    private final CacheManager cacheManager;

    public CacheInvalidationDomainService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public void invalidateProductCache(Long productId, String reason) {
        Cache productCache = cacheManager.getCache("product");
        if (productCache != null) {
            productCache.evict(productId);
        }

        Cache productDetailCache = cacheManager.getCache("productDetail");
        if (productDetailCache != null) {
            productDetailCache.evict(productId);
        }

        log.info("상품 캐시 무효화 완료 - ProductId: {}, Reason: {}", productId, reason);
    }

    public void invalidateProductListCache(String cacheKey, String reason) {
        Cache productListCache = cacheManager.getCache("productList");
        if (productListCache != null) {
            productListCache.evict(cacheKey);
        }

        log.info("상품 목록 캐시 무효화 완료 - CacheKey: {}, Reason: {}", cacheKey, reason);
    }

    public boolean isStockDepletionCacheInvalidationNeeded(int oldStock, int newStock) {
        return oldStock > 0 && newStock <= 0; // 재고가 있다가 소진된 경우
    }

    public boolean isLikeCacheInvalidationNeeded(int oldLikeCount, int newLikeCount) {
        return oldLikeCount != newLikeCount; // 좋아요 수가 변경된 경우
    }
}
