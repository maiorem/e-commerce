package com.loopers.application.payment;

import com.loopers.application.coupon.CouponProcessor;
import com.loopers.application.point.PointProcessor;
import com.loopers.application.product.StockDeductionProcessor;
import com.loopers.domain.order.OrderItemModel;
import com.loopers.domain.order.OrderItemRepository;
import com.loopers.domain.order.OrderModel;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.payment.PaymentHistoryModel;
import com.loopers.domain.payment.PaymentRepository;
import com.loopers.domain.payment.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentApplicationService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final PointProcessor pointProcessor;
    private final CouponProcessor couponProcessor;
    private final StockDeductionProcessor stockDeductionProcessor;

    /**
     * 결제 콜백 처리
     */
    @Transactional
    public void handlePaymentCallback(String transactionId, PaymentStatus status, String reason) {

        OrderModel order = orderRepository.findByTransactionKey(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 결제 트랜잭션 ID입니다: " + transactionId));

        List<OrderItemModel> orderItems = orderItemRepository.findByOrderId(order.getId());

        PaymentHistoryModel history = paymentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new IllegalArgumentException("결제 내역이 존재하지 않습니다: " + order.getId()));

        if (status == PaymentStatus.SUCCESS) {
            order.confirmPayment();
            history.success();

            // 쿠폰 사용 처리
            couponProcessor.useCoupon(order.getUserId(), order.getCouponCode());

        } else {
            order.cancelByPaymentFailure();
            history.fail();
            // 재고 복구
            stockDeductionProcessor.restoreProductStocks(orderItems);
            // 쿠폰 복구
            couponProcessor.restoreCoupon(order.getUserId(), order.getCouponCode());
            // 포인트 복구
            pointProcessor.restorePoint(order.getUserId(), order.getUsedPoints());

        }

    }

}
