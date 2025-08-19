package com.loopers.application.payment;

import com.loopers.domain.order.OrderModel;
import com.loopers.domain.order.OrderRepository;
import com.loopers.domain.order.OrderStatus;
import com.loopers.domain.payment.PaymentGatewayPort;
import com.loopers.domain.payment.PaymentQueryResult;
import com.loopers.domain.payment.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentMonitoringApplicationService {
    private final OrderRepository orderRepository;
    private final PaymentGatewayPort paymentGatewayPort;
    private final PaymentApplicationService paymentApplicationService;

    @Scheduled(fixedDelay = 60000) // 1분마다 결제 상태 점검
    @Transactional
    public void checkPendingPayments() {
        List<OrderModel> pendingOrders = orderRepository.findByStatusAndCreatedBefore(
                OrderStatus.PENDING,
                LocalDateTime.now().minusMinutes(5)
        );
        for (OrderModel order : pendingOrders) {
            try {
                PaymentQueryResult queryResult = paymentGatewayPort.queryPaymentStatus(order.getTransactionKey());
                if (queryResult.isQuerySuccess() && queryResult.status() != PaymentStatus.PENDING) {
                    paymentApplicationService.handlePaymentCallback(
                            order.getTransactionKey(),
                            queryResult.status(),
                            queryResult.reason() + " (스케줄러 확인)"
                    );
                    log.info("PENDING 주문 상태 업데이트: orderId={}, status={}",
                            order.getId(), queryResult.status());
                }
            } catch (Exception e) {
                log.error("결제 상태 확인 실패: orderId={}, error={}", order.getId(), e.getMessage());
            }
        }
    }


}
