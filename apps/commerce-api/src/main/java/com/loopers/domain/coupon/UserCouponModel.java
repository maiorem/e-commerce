package com.loopers.domain.coupon;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.user.UserId;
import jakarta.persistence.Embedded;
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

    @Embedded
    private UserId userId;

    private String couponCode;

    private UserCoupontStatus status = UserCoupontStatus.AVAILABLE;

    private LocalDate issuedAt;

    private LocalDate usedAt;

    public static UserCouponModel create(UserId userId, String couponCode) {
        UserCouponModel userCouponModel = new UserCouponModel();
        if (userId == null || couponCode == null || couponCode.isEmpty()) {
            throw new IllegalArgumentException("사용자 ID와 쿠폰 ID는 필수입니다.");
        }
        userCouponModel.userId = userId;
        userCouponModel.couponCode = couponCode;
        userCouponModel.status = UserCoupontStatus.AVAILABLE;;
        userCouponModel.issuedAt = LocalDate.now();
        return userCouponModel;
    }

    public boolean useCoupon(LocalDate usedAt) {
        if (this.status == UserCoupontStatus.USED) {
            throw new IllegalArgumentException("이미 사용된 쿠폰입니다.");
        }
        if (this.status == UserCoupontStatus.EXPIRED) {
            throw new IllegalArgumentException("만료된 쿠폰입니다.");
        }
        if (usedAt.isBefore(issuedAt)) {
            throw new IllegalArgumentException("쿠폰이 아직 발급되지 않았습니다.");
        }
        this.markAsUsed(usedAt);
        return true;
    }

    private void markAsUsed(LocalDate usedAt) {
        if (usedAt == null || usedAt.isBefore(issuedAt)) {
            throw new IllegalArgumentException("사용 날짜는 발급 날짜 이후여야 합니다.");
        }
        if (this.status == UserCoupontStatus.USED) {
            throw new IllegalArgumentException("이미 사용된 쿠폰입니다.");
        }
        this.status = UserCoupontStatus.USED;
        this.usedAt = usedAt;
    }

    public void reserve() {
        if (this.status != UserCoupontStatus.AVAILABLE) {
            throw new IllegalStateException("사용할 수 없는 쿠폰입니다.");
        }
        this.status = UserCoupontStatus.RESERVED;
    }

    public void completeUsage() {
        if (this.status != UserCoupontStatus.RESERVED) {
            throw new IllegalStateException("사용 완료 처리할 수 없는 쿠폰입니다.");
        }
        this.status = UserCoupontStatus.USED;
        this.usedAt = LocalDate.now();
    }

    public void cancelReservation() {
        if (this.status != UserCoupontStatus.RESERVED) {
            throw new IllegalStateException("예약 취소할 수 없는 쿠폰입니다.");
        }
        this.status = UserCoupontStatus.AVAILABLE;
    }


}
