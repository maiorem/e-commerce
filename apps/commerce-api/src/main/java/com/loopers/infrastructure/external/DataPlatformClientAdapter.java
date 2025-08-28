package com.loopers.infrastructure.external;

import com.loopers.domain.external.DataPlatformPort;
import com.loopers.domain.order.event.OrderCreatedEvent;
import com.loopers.domain.external.DataPlatformResult;
import com.loopers.domain.user.UserId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
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
            // 결제 방식과 쿠폰 코드는 이벤트에서 제공되지 않음
            orderData.put("order_date", event.getOrderDate().toString());
            orderData.put("sent_at", event.getOccurredAt().toString());

            log.debug("📊 [Data Platform] 전송 데이터: {}", orderData);

            String transactionKey = "TXN-" + System.currentTimeMillis();
            log.info("📊 [Data Platform] 주문 데이터 전송 완료 - transactionKey: {}", transactionKey);

            return DataPlatformResult.success(transactionKey);

        } catch (Exception e) {
            log.error("📊 [Data Platform] 주문 데이터 전송 실패 - OrderId: {}, Error: {}",
                    event.getOrderId(), e.getMessage(), e);
            return DataPlatformResult.failed("데이터 전송 실패: " + e.getMessage());
        }
    }

    @Override
    public void sendUserActionData(UserId userId, String action, Long targetId, ZonedDateTime timestamp) {
        log.info("📊 [Data Platform] 사용자 행동 데이터 전송 - UserId: {}, Action: {}, TargetId: {}",
                userId.getValue(), action, targetId);



    }
}
