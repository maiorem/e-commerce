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

        Map<Long, ProductModel> productMap = productRepository.findAllByIds(productIds)
                                                .stream()
                                                .collect(Collectors.toMap(ProductModel::getId, product -> product));

        List<ProductOutputInfo> productOutputInfoList = new ArrayList<>();

        for (Long productId : productIds) {
            ProductModel productModel = productMap.get(productId); 

            // 만약 좋아요 기록은 있으나 상품이 삭제되어 없을 경우 
            if (productModel == null) {
                continue; // 일단 스킵하고 다음 좋아요 기록으로 넘어감
            }

            BrandModel brandModel = null;
            if (productModel.getBrandId() != null) {
                brandModel = brandRepository.findById(productModel.getBrandId()).orElse(null);
            }
            
            CategoryModel categoryModel = null;
            if (productModel.getCategoryId() != null) {
                categoryModel = categoryRepository.findById(productModel.getCategoryId()).orElse(null);
            }

            int currentLikesCount = productModel.getLikesCount(); 

            ProductOutputInfo outputInfo = ProductOutputInfo.convertToInfo(productModel, brandModel, categoryModel, currentLikesCount);
            productOutputInfoList.add(outputInfo);
        }

        return productOutputInfoList;
        
    }

}
