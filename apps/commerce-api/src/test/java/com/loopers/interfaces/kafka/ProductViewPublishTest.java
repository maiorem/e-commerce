package com.loopers.interfaces.kafka;

import com.loopers.event.ProductViewedEvent;
import com.loopers.infrastructure.product.ProductDetailViewedPublisherImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProductViewPublishTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private ProductDetailViewedPublisherImpl productDetailViewedPublisher;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(productDetailViewedPublisher, "viewTopic", "view-events");
    }

    @Test
    void 상품_조회_이벤트가_카프카_토픽에_발행되는지_검증() {
        // given
        ProductViewedEvent event = ProductViewedEvent.createDetailView(1L, "user001");

        // when
        productDetailViewedPublisher.publish(event);

        // then
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ProductViewedEvent> eventCaptor = ArgumentCaptor.forClass(ProductViewedEvent.class);

        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), eventCaptor.capture());
        verify(applicationEventPublisher).publishEvent(event);
        
        assertThat(keyCaptor.getValue()).isEqualTo("1");
        assertThat(eventCaptor.getValue().getProductId()).isEqualTo(1L);
        assertThat(eventCaptor.getValue().getUserId()).isEqualTo("user001");
    }
}
