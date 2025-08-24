package com.loopers.domain.external;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class DataPlatformResult {

    private final boolean success;
    private final String message;
    private final String transactionKey;

    public static DataPlatformResult success(String transactionId) {
        return new DataPlatformResult(true, "데이터 전송 성공", transactionId);
    }

    public static DataPlatformResult failed(String message) {
        return new DataPlatformResult(false, message, null);
    }

}
