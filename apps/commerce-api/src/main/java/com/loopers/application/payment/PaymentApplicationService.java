package com.loopers.application.payment;

import com.loopers.application.coupon.CouponProcessor;
import com.loopers.application.order.OrderInfo;
import com.loopers.application.point.PointProcessor;
import com.loopers.application.product.StockDeductionProcessor;
import com.loopers.domain.order.OrderItemModel;
import com.loopers.domain.order.OrderItemRepository;
import com.loopers.domain.order.OrderModel;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.payment.*;
import com.loopers.domain.point.PointModel;
import com.loopers.domain.point.PointRepository;
import com.loopers.support.error.InsufficientPointException;
import com.loopers.support.error.PaymentFailedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentApplicationService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final PointRepository pointRepository;
    private final CardPaymentRepository cardPaymentRepository;
    private final PointPaymentRepository pointPaymentRepository;

    private final PointProcessor pointProcessor;
    private final CouponProcessor couponProcessor;
    private final StockDeductionProcessor stockDeductionProcessor;
    private final PaymentGatewayPort paymentGatewayPort;
    private final PaymentProcessor paymentProcessor;

    /**
     * 카드 결제 요청
     */
    public void processCardPayment(OrderInfo orderInfo, PaymentMethod paymentMethod, CardType cardType, String cardNumber) {
        try {
            log.info("카드 결제 요청 시작 - OrderId: {}, Amount: {}", orderInfo.orderId(), orderInfo.totalPrice().getAmount());
            
            PaymentData payment = PaymentData.create(orderInfo.orderId(), paymentMethod, cardType, cardNumber, orderInfo.totalPrice(), orderInfo.userId());

            PaymentResult result = paymentGatewayPort.processPayment(payment);
            
            if (result.isSuccess()) {
                log.info("카드 결제 요청 성공 - OrderId: {}, TransactionKey: {}", orderInfo.orderId(), result.transactionKey());
                
                PaymentModel paymentInfo = PaymentModel.create(orderInfo.orderId(), paymentMethod, orderInfo.totalPrice());
                paymentProcessor.save(paymentInfo);
                
                CardPayment cardPayment = CardPayment.create(paymentInfo, result.transactionKey(), cardType, cardNumber);
                cardPaymentRepository.save(cardPayment);
                
                log.info("카드 결제 정보 저장 완료 - OrderId: {}, PaymentId: {}", orderInfo.orderId(), paymentInfo.getId());
            } else {
                log.error("카드 결제 요청 실패 - OrderId: {}, TransactionKey: {}, Message: {}", 
                         orderInfo.orderId(), result.transactionKey(), result.message());
                throw new PaymentFailedException("카드 결제 요청에 실패했습니다: " + result.message());
            }
        } catch (PaymentFailedException e) {
            log.error("카드 결제 실패 - OrderId: {}, Error: {}", orderInfo.orderId(), e.getMessage());
            throw e; // PaymentFailedException은 다시 던짐
        } catch (Exception e) {
            log.error("카드 결제 요청 중 예상치 못한 오류 발생 - OrderId: {}, Error: {}", orderInfo.orderId(), e.getMessage(), e);
            throw new PaymentFailedException("카드 결제 처리 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 포인트 결제 요청
     */
    @Transactional
    public void processPointPayment(OrderInfo orderInfo, PaymentMethod paymentMethod, int requestPoints) {

        try {
            // 1. Payment 생성
            PaymentModel payment = PaymentModel.create(orderInfo.orderId(), paymentMethod, orderInfo.totalPrice());
            paymentProcessor.save(payment);

            // 2. 포인트 사용 처리
            int actualUsedPoints = pointProcessor.processPointUsage(orderInfo.userId(), orderInfo.totalPrice().getAmount(), requestPoints);

            // 3. 사용자 잔여 포인트 조회 및 업데이트
            PointModel pointModel = pointRepository.findByUserIdForRead(orderInfo.userId()).orElseThrow(() ->
                new IllegalArgumentException("사용자의 포인트 정보가 존재하지 않습니다: " + orderInfo.userId()));

            // 4. PointPayment 상세 정보 생성
            PointPayment pointPayment = PointPayment.create(payment, pointModel, actualUsedPoints);
            pointPayment.updateRemainingPoints(pointModel.getAmount());

            pointPaymentRepository.save(pointPayment);

            // 5. 결제 성공 처리
            payment.success();

            // 6. 주문 상태 확정
            OrderModel order = orderRepository.findById(orderInfo.orderId())
                    .orElseThrow(() -> new IllegalArgumentException("주문이 존재하지 않습니다: " + orderInfo.orderId()));
            order.confirm();

            log.info("포인트 결제 완료: orderId={}, usedPoints={}, remainingPoints={}",
                    orderInfo.orderId(), actualUsedPoints, pointModel.getAmount());

        } catch (InsufficientPointException e) {
            log.error("포인트 부족: orderId={}, requestPoints={}, error={}",
                    orderInfo.orderId(), requestPoints, e.getMessage());

            throw new PaymentFailedException("포인트가 부족합니다: " + e.getMessage());

        } catch (Exception e) {
            log.error("포인트 결제 중 오류 발생: orderId={}, error={}", orderInfo.orderId(), e.getMessage(), e);

            throw new PaymentFailedException("포인트 결제에 실패했습니다: " + e.getMessage());
        }
    }
    

    /**
     * 결제 콜백 처리
     */
    @Transactional
    public void handlePaymentCallback(String transactionKey, PaymentStatus status, String reason) {

        CardPayment cardPayment = cardPaymentRepository.findByTransactionKey(transactionKey)
                .orElseThrow(() -> new IllegalArgumentException("결제 내역이 존재하지 않습니다: " + transactionKey));

        PaymentModel payment = paymentRepository.findById(cardPayment.getPaymentId()).orElseThrow(()
                -> new IllegalArgumentException("결제 정보가 존재하지 않습니다: paymentId=" + cardPayment.getPaymentId()));

        OrderModel order = orderRepository.findById(payment.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("주문 내역이 존재하지 않습니다: " + payment.getOrderId()));

        List<OrderItemModel> orderItems = orderItemRepository.findByOrderId(order.getId());

        if (status == PaymentStatus.SUCCESS) {
            order.confirm();
            payment.success();

            // 쿠폰 사용 처리
            couponProcessor.useCoupon(order.getUserId(), order.getCouponCode());

        } else {
            order.cancel();
            payment.fail();
            // 재고 복구
            stockDeductionProcessor.restoreProductStocks(orderItems);
            // 쿠폰 복구
            couponProcessor.restoreCoupon(order.getUserId(), order.getCouponCode());

        }

    }

    /**
     * 결제 콜백 검증
     */
    public void validatePaymentCallback(String transactionKey) {

        CardPayment cardPayment = cardPaymentRepository.findByTransactionKey(transactionKey).orElseThrow(
                () -> new IllegalArgumentException("결제 내역이 존재하지 않습니다: " + transactionKey)
        );
        PaymentModel paymentModel = paymentRepository.findById(cardPayment.getPaymentId()).orElseThrow(
                () -> new IllegalArgumentException("결제 정보가 존재하지 않습니다: paymentId=" + cardPayment.getPaymentId()
        ));
        OrderModel orderModel = orderRepository.findById(paymentModel.getOrderId()).orElseThrow(
                () -> new IllegalArgumentException("주문 내역이 존재하지 않습니다: " + paymentModel.getOrderId()
        ));

        PaymentQueryResult queryResult = paymentGatewayPort.queryPaymentStatus(orderModel.getUserId().getValue(), transactionKey);
        if (!queryResult.isQuerySuccess()) {
            throw new IllegalArgumentException("결제 상태 조회 실패: " + queryResult.reason());
        }
        if (queryResult.status() != PaymentStatus.PENDING) {
            throw new IllegalArgumentException("이미 처리된 결제입니다: " + transactionKey);
        }

    }

    @Scheduled(fixedDelay = 600000) // 10분마다 결제 상태 점검
    @Transactional
    public void checkPendingPayments() {

        List<PaymentModel> pendingPayments = paymentRepository.findByStatusAndCreatedBefore(
                PaymentStatus.PENDING,
                ZonedDateTime.now().minusMinutes(5)
        );

        for (PaymentModel payment : pendingPayments) {
            CardPayment cardPay = cardPaymentRepository.findByPaymentId(payment.getId())
                    .orElseThrow(() -> new IllegalStateException("결제 정보가 존재하지 않습니다: paymentId=" + payment.getId()));
            OrderModel orderModel = orderRepository.findById(payment.getOrderId()).orElseThrow(
                    () -> new IllegalStateException("주문 정보가 존재하지 않습니다: orderId=" + payment.getOrderId()
                    ));
            try {
                PaymentQueryResult queryResult = paymentGatewayPort.queryPaymentStatus(orderModel.getUserId().getValue(), cardPay.getTransactionKey());

                if (queryResult.isQuerySuccess() && queryResult.status() != PaymentStatus.PENDING) {

                    handlePaymentCallback(
                            cardPay.getTransactionKey(),
                            queryResult.status(),
                            queryResult.reason() + " (스케줄러 확인)"
                    );
                    log.info("PENDING 결제 상태 업데이트: paymentId={}, orderId={}, status={}",
                            payment.getId(), payment.getOrderId(), queryResult.status());
                }
            } catch (Exception e) {
                log.error("결제 상태 확인 실패: paymentId={}, orderId={}, transactionKey={}, error={}",
                        payment.getId(), payment.getOrderId(), cardPay.getTransactionKey(), e.getMessage());
            }
        }
    }

    public void handlePaymentFailure(Long orderId) {
        try {
            OrderModel order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("주문이 존재하지 않습니다: " + orderId));
            order.cancel(); // 또는 FAILED 상태로 변경
            orderRepository.save(order);
            // 재고 복구
            List<OrderItemModel> orderItems = orderItemRepository.findByOrderId(order.getId());
            stockDeductionProcessor.restoreProductStocks(orderItems);
            // 쿠폰 복구
            couponProcessor.restoreCoupon(order.getUserId(), order.getCouponCode());

            log.info("결제 실패로 인한 주문 취소 완료 - OrderId: {}", orderId);
        } catch (Exception e) {
            log.error("결제 실패 처리 중 오류 발생 - OrderId: {}, Error: {}", orderId, e.getMessage(), e);
        }
    }
}
