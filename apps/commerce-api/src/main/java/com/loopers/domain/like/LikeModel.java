package com.loopers.domain.like;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.user.UserId;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name = "user_like")
@Getter
public class LikeModel extends BaseEntity {

    @Embedded
    private UserId userId;

    private Long productId;

    protected LikeModel() {}

    public static LikeModel of(UserId userId, Long productId) {
        LikeModel like = new LikeModel();
        like.userId = userId;
        like.productId = productId;
        return like;
    }

    public static LikeModel create(String userId, Long productId) {
        if (userId == null || productId == null) {
            throw new CoreException(ErrorType.NOT_FOUND, "사용자 ID와 상품 ID는 필수입니다.");
        }
        return LikeModel.of(UserId.of(userId), productId);
    }

}
