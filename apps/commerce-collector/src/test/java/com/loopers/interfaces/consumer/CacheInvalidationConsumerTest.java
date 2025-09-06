package com.loopers.interfaces.consumer;

import com.loopers.application.event.CacheInvalidationApplicationService;
import com.loopers.event.StockAdjustedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CacheInvalidationConsumerTest {

    @Mock
    private CacheInvalidationApplicationService cacheInvalidationService;

    @Mock
    private Acknowledgment acknowledgment;

    @InjectMocks
    private CacheInvalidationConsumer cacheInvalidationConsumer;

    @Test
    void 재고_조정_이벤트_수신시_캐시_무효화_서비스가_호출되는지_검증() {
        // given
        StockAdjustedEvent event = StockAdjustedEvent.create(1L, 100, 50);
        Map<String, Object> headers = new HashMap<>();
        headers.put("offset", 123L);
        headers.put("partition", 0);

        // when
        cacheInvalidationConsumer.handleStockAdjustedEvent(event, headers, acknowledgment);

        // then
        verify(cacheInvalidationService).handleStockAdjustedEvent(event);
        verify(acknowledgment).acknowledge();
    }
}
