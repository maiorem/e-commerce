package com.loopers.interfaces.api.payment;

import com.loopers.domain.payment.PaymentStatus;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

public class PaymentV1Dto {

    public record PaymentCallbackResponse(String transactionId, PaymentStatus status, String reason) {
        public PaymentCallbackResponse {
            if (transactionId == null || transactionId.isBlank()) {
                throw new CoreException(ErrorType.NOT_FOUND, "Transaction ID는 비어있을 수 없습니다.");
            }
            if (status == null ) {
                throw new CoreException(ErrorType.NOT_FOUND, "Status 값은 비어있을 수 없습니다.");
            }
        }
    }

    public record PaymentInfoResponse(
            Meta meta,
            Data data
    ) {
        public record Meta(
                String result,
                String errorCode,
                String message
        ) {}

        public record Data(
                String transactionKey,
                String orderId,
                String cardType,
                String cardNo,
                int amount,
                String status,
                String reason
        ) {
            public Data {
                if (transactionKey == null || transactionKey.isBlank()) {
                    throw new CoreException(ErrorType.NOT_FOUND, "Transaction Key는 비어있을 수 없습니다.");
                }
            }
        }
    }
}
