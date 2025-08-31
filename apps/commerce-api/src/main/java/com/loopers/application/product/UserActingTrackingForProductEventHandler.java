package com.loopers.application.product;


import com.loopers.domain.product.event.ProductClickedEvent;
import com.loopers.domain.product.event.ProductViewedEvent;
import com.loopers.domain.user.event.UserActionData;
import com.loopers.domain.user.event.UserActionTrackingPort;
import com.loopers.domain.user.event.UserActionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserActingTrackingForProductEventHandler {

    private final UserActionTrackingPort userActionTrackingPort;

    /**
     * 상품 조회 이벤트 처리
     */
    @EventListener
    @Async
    public void handleProductViewed(ProductViewedEvent event) {
        log.info("[ProductEventHandler] 상품 조회 이벤트 처리 - ProductId: {}, UserId: {}, ViewType: {}",
                event.getProductId(), event.getUserId().getValue(), event.getViewType());

        try {
            // 사용자 행동 추적
            String additionalInfo = buildViewAdditionalInfo(event);
            UserActionData actionData = UserActionData.create(
                    event.getUserId(),
                    UserActionType.PRODUCT_VIEW,
                    event.getProductId(),
                    additionalInfo
            );

            userActionTrackingPort.trackUserAction(actionData);

        } catch (Exception e) {
            log.error("[ProductEventHandler] 상품 조회 이벤트 처리 실패 - ProductId: {}, Error: {}",
                    event.getProductId(), e.getMessage(), e);
        }
    }

    /**
     * 상품 클릭 이벤트 처리
     */
    @EventListener
    @Async
    public void handleProductClicked(ProductClickedEvent event) {
        log.info("[ProductEventHandler] 상품 클릭 이벤트 처리 - ProductId: {}, UserId: {}, Context: {}",
                event.getProductId(), event.getUserId().getValue(), event.getClickContext());

        try {
            // 사용자 클릭 행동 추적
            String additionalInfo = buildClickAdditionalInfo(event);
            UserActionData actionData = UserActionData.create(
                    event.getUserId(),
                    UserActionType.PRODUCT_CLICK,
                    event.getProductId(),
                    additionalInfo
            );
            userActionTrackingPort.trackUserAction(actionData);

        } catch (Exception e) {
            log.error("[ProductEventHandler] 상품 클릭 이벤트 처리 실패 - ProductId: {}, Error: {}",
                    event.getProductId(), e.getMessage(), e);
        }
    }

    /**
     * 상품 조회 이벤트의 추가 정보 생성
     */
    private String buildViewAdditionalInfo(ProductViewedEvent event) {
        StringBuilder info = new StringBuilder();
        info.append("view_type=").append(event.getViewType());

        if (event.getReferrer() != null) {
            info.append(",referrer=").append(event.getReferrer());
        }

        return info.toString();
    }

    /**
     * 상품 클릭 이벤트의 추가 정보 생성
     */
    private String buildClickAdditionalInfo(ProductClickedEvent event) {
        StringBuilder info = new StringBuilder();
        info.append("click_context=").append(event.getClickContext());
        if (event.getPosition() != null) {
            info.append(",position=").append(event.getPosition());
        }
        return info.toString();
    }

}
