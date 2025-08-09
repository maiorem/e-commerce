package com.loopers.domain.like;

import com.loopers.domain.product.ProductModel;
import com.loopers.domain.user.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductLikeDomainService {

    private final LikeRepository likeRepository;

    /**
     * 상품에 좋아요 추가
     * @return 생성된 LikeModel, 이미 좋아요가 되어 있다면 null
     */
    public LikeModel addLike(ProductModel product, UserId userId) {
        // 이미 좋아요를 눌렀는지 확인
        if (isLiked(product.getId(), userId)) {
            return null; // 이미 좋아요가 되어 있으면 아무 동작도 하지 않음
        }

        // LikeModel 생성
        LikeModel like = LikeModel.create(userId, product.getId());
        
        // 상품의 좋아요 수 증가
        product.incrementLikesCount();
        
        return like;
    }

    /**
     * 상품 좋아요 제거
     * @return 제거된 LikeModel, 이미 좋아요가 취소되어 있다면 null
     */
    public LikeModel removeLike(ProductModel product, UserId userId) {
        // 좋아요를 누르지 않았는지 확인
        if (!isLiked(product.getId(), userId)) {
            return null; // 이미 좋아요가 취소되어 있으면 아무 동작도 하지 않음
        }

        // 기존 LikeModel 조회
        LikeModel like = likeRepository.findByUserIdAndProductId(userId, product.getId())
                .orElse(null);
        
        // 상품의 좋아요 수 감소
        product.decrementLikesCount();
        
        return like;
    }

    /**
     * 상품 좋아요 상태 확인
     */
    public boolean isLiked(Long productId, UserId userId) {
        return likeRepository.existsByUserIdAndProductId(userId, productId);
    }

    /**
     * 상품의 좋아요 수 조회
     */
    public int getLikeCount(Long productId) {
        return likeRepository.countByProductId(productId);
    }

    /**
     * 사용자가 좋아요한 상품 ID 목록 조회
     */
    public List<Long> getLikedProductIds(UserId userId) {
        return likeRepository.findByUserId(userId)
                .stream()
                .map(LikeModel::getProductId)
                .toList();
    }
} 
