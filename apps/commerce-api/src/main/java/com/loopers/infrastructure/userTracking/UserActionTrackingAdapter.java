package com.loopers.infrastructure.userTracking;

import com.loopers.domain.user.event.UserActionData;
import com.loopers.domain.user.event.UserActionTrackingPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class UserActionTrackingAdapter implements UserActionTrackingPort {


    @Override
    public void trackUserAction(UserActionData actionData) {
        try {
            log.info("📊 [UserActionTracking] 사용자 행동 추적 - UserId: {}, Action: {}, TargetId: {}",
                    actionData.getUserId().getValue(),
                    actionData.getActionType().getDescription(),
                    actionData.getTargetId());

            Map<String, Object> trackingData = new HashMap<>();
            trackingData.put("user_id", actionData.getUserId().getValue());
            trackingData.put("action_type", actionData.getActionType().name());
            trackingData.put("action_description", actionData.getActionType().getDescription());
            trackingData.put("target_id", actionData.getTargetId());
            trackingData.put("additional_info", actionData.getAdditionalInfo());
            trackingData.put("timestamp", actionData.getTimestamp().toString());
            trackingData.put("session_id", generateSessionId()); // 세션 추적
            trackingData.put("device_info", getDeviceInfo()); // 디바이스 정보

            log.debug("📊 [UserActionTracking] 추적 데이터: {}", trackingData);

            sendToAnalyticsPlatform(trackingData);

        } catch (Exception e) {
            log.error("📊 [UserActionTracking] 사용자 행동 추적 실패 - UserId: {}, Error: {}",
                    actionData.getUserId().getValue(), e.getMessage(), e);
        }
    }

    /**
     * 데이터 플랫폼으로 트래킹 정보 전송 (Mock)
     */
    private void sendToAnalyticsPlatform(Map<String, Object> trackingData) {
        log.info("ANALYTICS: {}", trackingData);
    }
    /**
     * 세션 ID 생성 (Mock)
     */
    private String generateSessionId() {
        return "SESSION-" + System.currentTimeMillis();
    }

    /**
     * 디바이스 정보 추출 (Mock)
     */
    private String getDeviceInfo() {
        return "WEB";
    }

}
