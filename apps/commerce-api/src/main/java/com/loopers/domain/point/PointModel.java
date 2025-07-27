package com.loopers.domain.point;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.user.UserId;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "point")
public class PointModel extends BaseEntity {

    @Embedded
    private UserId userId;

    private int amount;

    protected PointModel() {}

    public static PointModel of(UserId userId, int amount) {
        if (userId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "사용자 ID는 비어있을 수 없습니다.");
        }
        if (amount < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "포인트는 음수가 될 수 없습니다.");
        }
        PointModel point = new PointModel();
        point.userId = userId;
        point.amount = amount;
        return point;
    }

    public UserId getUserId() {
        return userId;
    }

    public int getAmount() {
        return amount;
    }

    public int addPoint(int point) {
        if (point <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "추가할 포인트는 0보다 커야 합니다.");
        }
        this.amount += point;
        return this.amount;
    }

    public int removePoint(int point) {
        if (point <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "차감할 포인트는 0보다 커야 합니다.");
        }
        if (this.amount < point) {
            throw new CoreException(ErrorType.BAD_REQUEST, "잔여 포인트가 부족합니다.");
        }
        this.amount -= point;
        return this.amount;
    }
}

