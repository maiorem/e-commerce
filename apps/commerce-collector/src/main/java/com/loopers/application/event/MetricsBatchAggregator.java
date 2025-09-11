package com.loopers.application.event;

import com.loopers.event.LikeChangedEvent;
import com.loopers.event.OrderCreatedEvent;
import com.loopers.event.ProductViewedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
@RequiredArgsConstructor
public class MetricsBatchAggregator {
    
    private final ConsumerEventMapper eventMapper;
    
    public ProductMetricsAggregation aggregateEvents(List<Map<String, Object>> eventDataList) {
        log.debug("이벤트 배치 집계 시작 - 이벤트 수: {}", eventDataList.size());
        
        // 집계용 Map들 (Thread-safe)
        Map<Long, AtomicInteger> viewCounts = new ConcurrentHashMap<>();
        Map<Long, ZonedDateTime> viewTimestamps = new ConcurrentHashMap<>();
        Map<Long, Long> latestLikeCounts = new ConcurrentHashMap<>();
        Map<Long, ZonedDateTime> likeTimestamps = new ConcurrentHashMap<>();
        Map<Long, AtomicInteger> salesCounts = new ConcurrentHashMap<>();
        Map<Long, AtomicLong> salesAmounts = new ConcurrentHashMap<>();
        
        Set<String> processedEventIds = new HashSet<>();
        
        for (Map<String, Object> eventData : eventDataList) {
            String eventId = (String) eventData.get("eventId");
            
            // 중복 이벤트 스킵
            if (!processedEventIds.add(eventId)) {
                log.debug("중복 이벤트 스킵 - EventId: {}", eventId);
                continue;
            }
            
            String eventType = determineEventType(eventData);
            
            switch (eventType) {
                case "VIEW" -> processViewEvent(eventData, viewCounts, viewTimestamps);
                case "LIKE" -> processLikeEvent(eventData, latestLikeCounts, likeTimestamps);
                case "ORDER" -> processOrderEvent(eventData, salesCounts, salesAmounts);
                default -> log.warn("알 수 없는 이벤트 타입: {}", eventType);
            }
        }
        
        ProductMetricsAggregation aggregation = buildAggregation(
            viewCounts, viewTimestamps,
            latestLikeCounts, likeTimestamps,
            salesCounts, salesAmounts
        );
        
        log.debug("이벤트 배치 집계 완료 - View: {}, Like: {}, Sales: {}", 
                 aggregation.getViewCounts().size(),
                 aggregation.getLikeUpdates().size(),
                 aggregation.getSalesData().size());
        
        return aggregation;
    }
    
    private String determineEventType(Map<String, Object> eventData) {
        // 이벤트 타입 결정 로직
        if (eventData.containsKey("productId") && eventData.containsKey("userId") 
            && !eventData.containsKey("changeType") && !eventData.containsKey("orderItems")) {
            return "VIEW";
        } else if (eventData.containsKey("changeType")) {
            return "LIKE";
        } else if (eventData.containsKey("orderItems")) {
            return "ORDER";
        }
        return "UNKNOWN";
    }
    
    private void processViewEvent(Map<String, Object> eventData,
                                 Map<Long, AtomicInteger> viewCounts,
                                 Map<Long, ZonedDateTime> viewTimestamps) {
        try {
            ProductViewedEvent event = eventMapper.toProductViewedEvent(eventData);
            Long productId = event.getProductId();
            
            // 조회수 집계
            viewCounts.computeIfAbsent(productId, k -> new AtomicInteger(0))
                     .incrementAndGet();
            
            // 최신 조회 시간 업데이트
            viewTimestamps.merge(productId, event.getOccurredAt(),
                (existing, incoming) -> incoming.isAfter(existing) ? incoming : existing);
                
            log.debug("조회 이벤트 집계 - ProductId: {}", productId);
        } catch (Exception e) {
            log.error("조회 이벤트 처리 실패: {}", e.getMessage(), e);
        }
    }
    
    private void processLikeEvent(Map<String, Object> eventData,
                                 Map<Long, Long> latestLikeCounts,
                                 Map<Long, ZonedDateTime> likeTimestamps) {
        try {
            LikeChangedEvent event = eventMapper.toLikeChangedEvent(eventData);
            
            // LIKE 타입만 처리
            if ("LIKE".equals(event.getChangeType())) {
                Long productId = event.getProductId();
                
                // 최종 좋아요 수 (배치 내 최신값)
                latestLikeCounts.put(productId, (long) event.getNewLikeCount());
                likeTimestamps.merge(productId, event.getOccurredAt(),
                    (existing, incoming) -> incoming.isAfter(existing) ? incoming : existing);
                    
                log.debug("좋아요 이벤트 집계 - ProductId: {}, LikeCount: {}", 
                         productId, event.getNewLikeCount());
            }
        } catch (Exception e) {
            log.error("좋아요 이벤트 처리 실패: {}", e.getMessage(), e);
        }
    }
    
    private void processOrderEvent(Map<String, Object> eventData,
                                  Map<Long, AtomicInteger> salesCounts,
                                  Map<Long, AtomicLong> salesAmounts) {
        try {
            OrderCreatedEvent event = eventMapper.toOrderCreatedEvent(eventData);
            
            event.getOrderItems().forEach(item -> {
                Long productId = item.getProductId();
                long itemAmount = (long) item.getPrice() * item.getQuantity();
                
                // 판매 건수 집계
                salesCounts.computeIfAbsent(productId, k -> new AtomicInteger(0))
                          .incrementAndGet();
                
                // 판매 금액 집계
                salesAmounts.computeIfAbsent(productId, k -> new AtomicLong(0))
                           .addAndGet(itemAmount);
                           
                log.debug("주문 이벤트 집계 - ProductId: {}, Amount: {}", productId, itemAmount);
            });
        } catch (Exception e) {
            log.error("주문 이벤트 처리 실패: {}", e.getMessage(), e);
        }
    }
    
    private ProductMetricsAggregation buildAggregation(
            Map<Long, AtomicInteger> viewCounts,
            Map<Long, ZonedDateTime> viewTimestamps,
            Map<Long, Long> latestLikeCounts,
            Map<Long, ZonedDateTime> likeTimestamps,
            Map<Long, AtomicInteger> salesCounts,
            Map<Long, AtomicLong> salesAmounts) {
        
        // ViewCount 변환
        Map<Long, ProductMetricsAggregation.ViewCount> viewCountMap = new ConcurrentHashMap<>();
        viewCounts.forEach((productId, count) -> {
            viewCountMap.put(productId, ProductMetricsAggregation.ViewCount.builder()
                    .productId(productId)
                    .count(count.get())
                    .lastViewedAt(viewTimestamps.get(productId))
                    .build());
        });
        
        // LikeUpdate 변환
        Map<Long, ProductMetricsAggregation.LikeUpdate> likeUpdateMap = new ConcurrentHashMap<>();
        latestLikeCounts.forEach((productId, likeCount) -> {
            likeUpdateMap.put(productId, ProductMetricsAggregation.LikeUpdate.builder()
                    .productId(productId)
                    .finalLikeCount(likeCount)
                    .lastLikedAt(likeTimestamps.get(productId))
                    .build());
        });
        
        // SalesData 변환
        Map<Long, ProductMetricsAggregation.SalesData> salesDataMap = new ConcurrentHashMap<>();
        salesCounts.forEach((productId, count) -> {
            Long totalAmount = salesAmounts.get(productId).get();
            salesDataMap.put(productId, ProductMetricsAggregation.SalesData.builder()
                    .productId(productId)
                    .salesCount(count.get())
                    .totalAmount(totalAmount)
                    .build());
        });
        
        return ProductMetricsAggregation.builder()
                .viewCounts(viewCountMap)
                .likeUpdates(likeUpdateMap)
                .salesData(salesDataMap)
                .build();
    }
}
