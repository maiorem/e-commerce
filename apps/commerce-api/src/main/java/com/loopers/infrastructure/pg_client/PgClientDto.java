package com.loopers.infrastructure.pg_client;

import com.loopers.domain.payment.CardType;
import com.loopers.domain.payment.PaymentData;

public class PgClientDto {

    public record PgClientRequest(
            String orderId,
            CardType cardType,
            String cardNumber,
            Long finalTotalPrice,
            String calbackUrl
    ) {
        public static PgClientRequest from(PaymentData paymentData, String callbackUrl) {
            return new PgClientRequest(
                    paymentData.orderId().toString(),
                    paymentData.cardType(),
                    paymentData.cardNumber(),
                    (long) paymentData.finalTotalPrice(),
                    callbackUrl
            );
        }

    }

    public record PgClientResponse(
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
                String status,
                String reason
        ) {}
    }
}
