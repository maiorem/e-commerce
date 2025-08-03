package com.loopers.domain.point;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.user.UserId;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name = "point_history")
@Getter
public class PointHistoryModel extends BaseEntity {

    @Embedded
    private UserId userId;

    private int changedAmount;
    private int currentAmount;

    @Enumerated(EnumType.STRING)
    private PointChangeReason reason;

    protected PointHistoryModel() {}

    public static PointHistoryModel of(UserId userId, int changedAmount, int currentAmount, PointChangeReason reason) {
        PointHistoryModel history = new PointHistoryModel();
        history.userId = userId;
        history.changedAmount = changedAmount;
        history.currentAmount = currentAmount;
        history.reason = reason;
        return history;
    }
} 