package com.loopers.interfaces.api.payment;

import com.loopers.domain.payment.PaymentStatus;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

public class PaymentV1Dto {

    public record PaymentCallbackRequest(
            String transactionKey,
            PaymentStatus status,
            String reason
    ) {
        public PaymentCallbackRequest {
            if (transactionKey == null || transactionKey.isBlank()) {
                throw new CoreException(ErrorType.BAD_REQUEST, "Transaction Key는 비어있을 수 없습니다.");
            }
            if (status == null) {
                throw new CoreException(ErrorType.BAD_REQUEST, "Status 값은 비어있을 수 없습니다.");
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
