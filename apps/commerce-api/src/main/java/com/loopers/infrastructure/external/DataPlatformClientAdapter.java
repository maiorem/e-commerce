package com.loopers.infrastructure.external;

import com.loopers.domain.external.DataPlatformPort;
import com.loopers.domain.external.DataPlatformResult;
import com.loopers.domain.order.event.OrderCreatedEvent;
import com.loopers.domain.payment.event.PaymentFailedEvent;
import com.loopers.domain.payment.event.PaymentSuccessEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class DataPlatformClientAdapter implements DataPlatformPort {

    @Override
    public DataPlatformResult sendOrderData(OrderCreatedEvent event) {
        try {
            log.info("📊 [Data Platform] 주문 데이터 전송 - OrderId: {}, UserId: {}, Amount: {}",
                    event.getOrderId(), event.getUserId().getValue(), event.getTotalAmount().getAmount());

            Map<String, Object> orderData = new HashMap<>();
            orderData.put("order_id", event.getOrderId());
            orderData.put("order_number", event.getOrderNumber());
            orderData.put("user_id", event.getUserId().getValue());
            orderData.put("amount", event.getTotalAmount().getAmount());
            orderData.put("order_date", event.getOrderDate().toString());
            orderData.put("sent_at", event.getOccurredAt().toString());

            log.debug("📊 [Data Platform] 전송 데이터: {}", orderData);

            log.info("📊 [Data Platform] 주문 데이터 전송 완료 - order_id: {}", event.getOrderId());

            return DataPlatformResult.success(null);

        } catch (Exception e) {
            log.error("📊 [Data Platform] 주문 데이터 전송 실패 - OrderId: {}, Error: {}",
                    event.getOrderId(), e.getMessage(), e);
            return DataPlatformResult.failed("데이터 전송 실패: " + e.getMessage());
        }
    }

    @Override
    public DataPlatformResult sendPaymentSuccess(PaymentSuccessEvent event) {
        try {
            log.info("📊 [Data Platform] 결제 성공 결과 데이터 전송 - OrderId: {}, PaymentId: {}, PaymentMethod: {}",
                    event.getOrderId(), event.getPaymentId(), event.getPaymentMethod());

            Map<String, Object> paymentData = new HashMap<>();
            paymentData.put("order_id", event.getOrderId());
            paymentData.put("payment_id", event.getPaymentId());
            paymentData.put("payment_method", event.getPaymentMethod());
            paymentData.put("sent_at", event.getOccurredAt().toString());

            log.debug("📊 [Data Platform] 전송 데이터: {}", paymentData);

            log.info("📊 [Data Platform] 결제 성공 결과 데이터 전송 완료 - transactionKey: {}", event.getTransactionKey());

            return DataPlatformResult.success(event.getTransactionKey());

        } catch (Exception e) {
            log.error("📊 [Data Platform] 결제 결과 데이터 전송 실패 - OrderId: {}, Error: {}",
                    event.getOrderId(), e.getMessage(), e);
            return DataPlatformResult.failed("데이터 전송 실패: " + e.getMessage());
        }
    }

    @Override
    public DataPlatformResult sendPaymentFailure(PaymentFailedEvent event) {
        try {
            log.info("📊 [Data Platform] 결제 실패 결과 데이터 전송 - OrderId: {}, PaymentMethod: {}, Reason: {}",
                    event.getOrderId(), event.getPaymentMethod(), event.getFailureReason());

            Map<String, Object> paymentData = new HashMap<>();
            paymentData.put("order_id", event.getOrderId());
            paymentData.put("payment_method", event.getPaymentMethod());
            paymentData.put("failure_reason", event.getFailureReason());
            paymentData.put("sent_at", event.getOccurredAt().toString());

            log.debug("📊 [Data Platform] 전송 데이터: {}", paymentData);

            return DataPlatformResult.success(null);

        } catch (Exception e) {
            log.error("📊 [Data Platform] 결제 결과 데이터 전송 실패 - OrderId: {}, Error: {}",
                    event.getOrderId(), e.getMessage(), e);
            return DataPlatformResult.failed("데이터 전송 실패: " + e.getMessage());
        }
    }

}
