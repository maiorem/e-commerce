package com.loopers.interfaces.consumer;

import com.loopers.application.event.AuditLogApplicationService;
import com.loopers.config.kafka.KafkaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditLogConsumer {

    private final AuditLogApplicationService auditLogService;

    /**
     * 모든 이벤트를 감사 로그로 저장 (배치 처리)
     */
    @KafkaListener(
            topics = {"stock-events", "like-events", "view-events", "order-events"},
            groupId = "audit-log-group",
            containerFactory = KafkaConfig.BATCH_LISTENER
    )
    public void handleAuditEvents(
            List<ConsumerRecord<String, Object>> records,
            Acknowledgment ack
    ) {
        log.info("감사 로그 배치 처리 시작 - 메시지 수: {}", records.size());

        // 토픽별 분포 로깅 (모니터링용)
        Map<String, Long> topicDistribution = records.stream()
                .collect(Collectors.groupingBy(
                        ConsumerRecord::topic,
                        Collectors.counting()
                ));
        log.info("토픽별 메시지 분포: {}", topicDistribution);

        try {
            auditLogService.handleEventsBatch(records);
            ack.acknowledge();

            log.info("감사 로그 배치 처리 완료");

        } catch (Exception e) {
            log.error("감사 로그 배치 처리 실패: {}", e.getMessage(), e);
            throw e; // DLQ로 전송하기 위해 예외 재발생
        }
    }




}
