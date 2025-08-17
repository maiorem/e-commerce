package com.loopers.application.like;

import com.loopers.application.product.ProductOutputInfo;
import com.loopers.domain.brand.BrandModel;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.category.CategoryModel;
import com.loopers.domain.category.CategoryRepository;
import com.loopers.domain.like.LikeModel;
import com.loopers.domain.like.LikeRepository;
import com.loopers.domain.like.ProductLikeDomainService;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.user.UserId;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.hibernate.StaleObjectStateException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LikeApplicationService {

    private final LikeRepository likeRepository;
    private final ProductRepository productRepository;
    private final ProductLikeDomainService productLikeDomainService;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;

    /**
     * 사용자가 상품을 좋아요 추가
     */
    @Retryable(retryFor = {OptimisticLockException.class, StaleObjectStateException.class,
            ObjectOptimisticLockingFailureException.class}, maxAttempts = 10, backoff = @Backoff(delay = 100))
    @Transactional
    public void like(UserId userId, Long productId) {
        ProductModel product = productRepository.findById(productId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품이 존재하지 않습니다."));

        // 도메인 서비스를 통한 좋아요 추가
        LikeModel like = productLikeDomainService.addLike(product, userId);

        // null이 반환되면 이미 좋아요가 되어 있는 상태이므로 아무 동작도 하지 않음
        if (like != null) {
            likeRepository.save(like);
        }
    }

    /**
     * 사용자가 상품을 좋아요 제거
     */
    @Retryable(retryFor = {OptimisticLockException.class, StaleObjectStateException.class,
            ObjectOptimisticLockingFailureException.class}, maxAttempts = 10, backoff = @Backoff(delay = 100))
    @Transactional
    public void unlike(UserId userId, Long productId) {
        ProductModel product = productRepository.findById(productId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품이 존재하지 않습니다."));

        // 도메인 서비스를 통한 좋아요 제거
        LikeModel like = productLikeDomainService.removeLike(product, userId);

        // null이 반환되면 이미 좋아요가 취소되어 있는 상태이므로 아무 동작도 하지 않음
        if (like != null) {
            likeRepository.delete(like);
        }
    }

    /**
     * 사용자가 상품을 좋아요 했는지 확인
     */
    public boolean isLiked(UserId userId, Long productId) {
        return productLikeDomainService.isLiked(productId, userId);
    }

    /**
     * 사용자의 좋아요 목록 조회
     */
    public List<ProductOutputInfo> getLikedProducts(UserId userId) {

        // 좋아요한 상품 ID 목록 조회
        List<Long> productIds = productLikeDomainService.getLikedProductIds(userId);

        if (productIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 모든 상품 정보를 한 번에 조회
        Map<Long, ProductModel> productMap = productRepository.findAllByIds(productIds)
                .stream()
                .collect(Collectors.toMap(ProductModel::getId, product -> product));

        // 모든 브랜드 ID와 카테고리 ID를 수집
        Set<Long> brandIds = productMap.values().stream()
                .map(ProductModel::getBrandId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> categoryIds = productMap.values().stream()
                .map(ProductModel::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 모든 브랜드/카테고리 정보를 한 번에 조회
        Map<Long, BrandModel> brandMap = brandRepository.findAllById(brandIds).stream()
                .collect(Collectors.toMap(BrandModel::getId, brand -> brand));
        Map<Long, CategoryModel> categoryMap = categoryRepository.findAllById(categoryIds).stream()
                .collect(Collectors.toMap(CategoryModel::getId, category -> category));

        List<ProductOutputInfo> productOutputInfoList = new ArrayList<>();

        for (Long productId : productIds) {
            ProductModel productModel = productMap.get(productId);

            // 만약 좋아요 기록은 있으나 상품이 삭제되어 없을 경우 
            if (productModel == null) {
                continue; // 일단 스킵하고 다음 좋아요 기록으로 넘어감
            }

            BrandModel brandModel = brandMap.get(productModel.getBrandId());
            CategoryModel categoryModel = categoryMap.get(productModel.getCategoryId());

            ProductOutputInfo outputInfo = ProductOutputInfo.convertToInfo(productModel, brandModel, categoryModel);
            productOutputInfoList.add(outputInfo);
        }

        return productOutputInfoList;

    }

}
