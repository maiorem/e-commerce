package com.loopers.domain.coupon;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Entity
@Table(name = "user_coupon")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCouponModel extends BaseEntity {

    private Long userId;
    private Long couponId;
    private boolean isUsed;

    private LocalDate issuedAt;
    private LocalDate usedAt;

    public static UserCouponModel of(Long userId, Long couponId) {
        UserCouponModel userCouponModel = new UserCouponModel();
        if (userId == null || couponId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "사용자 ID와 쿠폰 ID는 필수입니다.");
        }
        userCouponModel.userId = userId;
        userCouponModel.couponId = couponId;
        userCouponModel.isUsed = false;
        return userCouponModel;
    }


    public boolean useCoupon(LocalDate usedAt) {
        if (this.isUsed) {
            throw new CoreException(ErrorType.BAD_REQUEST, "이미 사용된 쿠폰입니다.");
        }
        if (usedAt.isBefore(issuedAt)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "쿠폰이 아직 발급되지 않았습니다.");
        }
        this.markAsUsed(usedAt);
        return true;
    }

    private void markAsUsed(LocalDate usedAt) {
        if (usedAt == null || usedAt.isBefore(issuedAt)) {
            throw new CoreException(ErrorType.BAD_REQUEST, "사용 날짜는 발급 날짜 이후여야 합니다.");
        }
        if (this.isUsed) {
            throw new CoreException(ErrorType.BAD_REQUEST, "이미 사용된 쿠폰입니다.");
        }
        this.isUsed = true;
        this.usedAt = usedAt;
    }

}
