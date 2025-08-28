package com.loopers.domain.payment.model;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.point.PointModel;
import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "point_payment")
public class PointPayment extends BaseEntity {

    private Long paymentId;

    private Long pointId;

    private int usedPoints;

    private int remainingPoints;

    protected PointPayment() {
    }

    public static PointPayment create(PaymentModel payment, PointModel point, int usedPoints) {
        PointPayment pointPayment = new PointPayment();
        pointPayment.paymentId = payment.getId();
        pointPayment.pointId = point.getId();
        pointPayment.usedPoints = usedPoints;
        return pointPayment;
    }

    public void updateRemainingPoints(int remainingPoints) {
        this.remainingPoints = remainingPoints;
    }

}
