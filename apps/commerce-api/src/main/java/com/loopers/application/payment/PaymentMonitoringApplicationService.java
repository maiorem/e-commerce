package com.loopers.application.payment;

import com.loopers.domain.payment.*;
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
    private final PaymentRepository paymentRepository;
    private final PaymentGatewayPort paymentGatewayPort;
    private final PaymentApplicationService paymentApplicationService;

    @Scheduled(fixedDelay = 600000) // 10분마다 결제 상태 점검
    @Transactional
    public void checkPendingPayments() {
        // PaymentModel에서 PENDING 상태인 것들 조회
        List<PaymentModel> pendingPayments = paymentRepository.findByStatusAndCreatedBefore(
                PaymentStatus.PENDING,
                LocalDateTime.now().minusMinutes(5)
        );

        for (PaymentModel payment : pendingPayments) {
            try {
                // transactionKey로 PG 상태 조회
                PaymentQueryResult queryResult = paymentGatewayPort.queryPaymentStatus(payment.getTransactionKey());

                if (queryResult.isQuerySuccess() && queryResult.status() != PaymentStatus.PENDING) {
                    // 상태가 변경되었으면 콜백 처리 로직 실행
                    paymentApplicationService.handlePaymentCallback(
                            payment.getTransactionKey(),
                            queryResult.status(),
                            queryResult.reason() + " (스케줄러 확인)"
                    );
                    log.info("PENDING 결제 상태 업데이트: paymentId={}, orderId={}, status={}",
                            payment.getId(), payment.getOrderId(), queryResult.status());
                }
            } catch (Exception e) {
                log.error("결제 상태 확인 실패: paymentId={}, orderId={}, transactionKey={}, error={}",
                        payment.getId(), payment.getOrderId(), payment.getTransactionKey(), e.getMessage());
            }
        }
    }


}
