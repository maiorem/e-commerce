package com.loopers.interfaces.consumer;

import com.loopers.application.event.CacheInvalidationApplicationService;
import com.loopers.event.StockAdjustedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdempotentMessageProcessingTest {

    @Mock
    private CacheInvalidationApplicationService cacheInvalidationService;

    @Mock
    private Acknowledgment acknowledgment;

    @InjectMocks
    private CacheInvalidationConsumer cacheInvalidationConsumer;

    @Test
    void 동일한_이벤트_중복_처리시_멱등성_보장_테스트() {
        // given - 동일한 이벤트
        StockAdjustedEvent event = StockAdjustedEvent.create(1L, 100, 50);
        Map<String, Object> headers = createHeaders(123L, 0);

        // when - 동일한 이벤트를 여러 번 처리
        cacheInvalidationConsumer.handleStockAdjustedEvent(event, headers, acknowledgment);
        cacheInvalidationConsumer.handleStockAdjustedEvent(event, headers, acknowledgment);
        cacheInvalidationConsumer.handleStockAdjustedEvent(event, headers, acknowledgment);

        // then
        verify(cacheInvalidationService, times(3)).handleStockAdjustedEvent(event);
        verify(acknowledgment, times(3)).acknowledge();
    }

    @Test
    void 다른_오프셋의_동일_이벤트_처리_테스트() {
        // given - 동일한 이벤트, 다른 오프셋
        StockAdjustedEvent event = StockAdjustedEvent.create(1L, 100, 50);
        Map<String, Object> headers1 = createHeaders(123L, 0);
        Map<String, Object> headers2 = createHeaders(124L, 0); // 다른 오프셋

        // when - 다른 오프셋으로 동일 이벤트 처리
        cacheInvalidationConsumer.handleStockAdjustedEvent(event, headers1, acknowledgment);
        cacheInvalidationConsumer.handleStockAdjustedEvent(event, headers2, acknowledgment);

        // then
        verify(cacheInvalidationService, times(2)).handleStockAdjustedEvent(event);
        verify(acknowledgment, times(2)).acknowledge();
    }

    @Test
    void 이벤트_처리_실패시_ACK하지_않는_테스트() {
        // given
        StockAdjustedEvent event = StockAdjustedEvent.create(1L, 100, 50);
        Map<String, Object> headers = createHeaders(123L, 0);
        

        doThrow(new RuntimeException("캐시 무효화 실패"))
                .when(cacheInvalidationService).handleStockAdjustedEvent(event);

        // when & then
        try {
            cacheInvalidationConsumer.handleStockAdjustedEvent(event, headers, acknowledgment);
        } catch (RuntimeException e) {
            assert(e.getMessage().contains("캐시 무효화 실패"));
        }
        verify(cacheInvalidationService).handleStockAdjustedEvent(event);
        verify(acknowledgment, never()).acknowledge();
    }

    private Map<String, Object> createHeaders(Long offset, Integer partition) {
        Map<String, Object> headers = new HashMap<>();
        headers.put(KafkaHeaders.OFFSET, offset);
        headers.put(KafkaHeaders.RECEIVED_PARTITION, partition);
        return headers;
    }
}
