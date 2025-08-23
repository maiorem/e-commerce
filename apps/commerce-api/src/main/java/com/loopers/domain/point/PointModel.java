package com.loopers.domain.point;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.user.UserId;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "point")
public class PointModel extends BaseEntity {

    @Embedded
    private UserId userId;

    private int amount;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    protected PointModel() {}

    public static PointModel of(UserId userId, int amount) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 비어있을 수 없습니다.");
        }
        if (amount < 0) {
            throw new IllegalArgumentException("포인트는 음수가 될 수 없습니다.");
        }

        PointModel point = new PointModel();
        point.userId = userId;
        point.amount = amount;
        point.expiredAt = LocalDateTime.now().plusYears(1); // 기본 1년 후 만료
        return point;
    }

    public static PointModel of(UserId userId, int amount, LocalDateTime expiredAt) {
        if (userId == null) {
            throw new IllegalArgumentException("사용자 ID는 비어있을 수 없습니다.");
        }
        if (amount < 0) {
            throw new IllegalArgumentException("포인트는 음수가 될 수 없습니다.");
        }
        if (expiredAt == null) {
            throw new IllegalArgumentException("만료일은 필수입니다.");
        }

        PointModel point = new PointModel();
        point.userId = userId;
        point.amount = amount;
        point.expiredAt = expiredAt;
        return point;
    }

    public UserId getUserId() {
        return userId;
    }

    public int getAmount() {
        return amount;
    }

    public LocalDateTime getExpiredAt() {
        return expiredAt;
    }

    public int charge(int point) {
        if (point <= 0) {
            throw new IllegalArgumentException("추가할 포인트는 0보다 커야 합니다.");
        }
        this.amount += point;
        return this.amount;
    }

    public int use(int point) {
        if (point <= 0) {
            throw new IllegalArgumentException("차감할 포인트는 0보다 커야 합니다.");
        }
        if (this.amount < point) {
            throw new IllegalArgumentException("잔여 포인트가 부족합니다.");
        }
        this.amount -= point;
        return this.amount;
    }

    public boolean isExpired() {
        return expiredAt != null && LocalDateTime.now().isAfter(expiredAt);
    }

    public void restorePoint(int usedPoints) {
        if (usedPoints <= 0) {
            throw new IllegalArgumentException("복원할 포인트는 0보다 커야 합니다.");
        }
        this.amount += usedPoints;
    }
}

