package com.loopers.application.like;

import com.loopers.domain.like.event.ProductLikedEvent;
import com.loopers.domain.like.event.ProductUnlikedEvent;
import com.loopers.domain.user.event.UserActionData;
import com.loopers.domain.user.event.UserActionTrackingPort;
import com.loopers.domain.user.event.UserActionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserLikeActionTrackingEventHandler {

    private final UserActionTrackingPort userActionTrackingPort;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void trackUserLikeAction(ProductLikedEvent event) {
        try {
            UserActionData actionData = UserActionData.create(
                    event.getUserId(),
                    UserActionType.PRODUCT_LIKE,
                    event.getProductId()
            );
            userActionTrackingPort.trackUserAction(actionData);

        } catch (Exception e) {
            log.error("[LikeEventHandler] Like Count 사용자 행동 로깅 실패 - ProductId: {}, UserId: {}, Error: {}",
                    event.getProductId(), event.getUserId().getValue(), e.getMessage(), e);
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void trackUserUnlikeAction(ProductUnlikedEvent event) {
        try {
            UserActionData actionData = UserActionData.create(
                    event.getUserId(),
                    UserActionType.PRODUCT_UNLIKE,
                    event.getProductId()
            );
            userActionTrackingPort.trackUserAction(actionData);

        } catch (Exception e) {
            log.error("[LikeEventHandler] UnLike Count 사용자 행동 로깅 실패 - ProductId: {}, UserId: {}, Error: {}",
                    event.getProductId(), event.getUserId().getValue(), e.getMessage(), e);
        }
    }
}
