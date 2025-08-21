package com.loopers.infrastructure.http;

import com.loopers.domain.order.Money;
import com.loopers.domain.payment.CardType;
import com.loopers.domain.payment.PaymentData;

import java.util.List;

public class PgClientDto {

    public record PgClientRequest(
            String orderId,
            CardType cardType,
            String cardNumber,
            Money finalTotalPrice,
            String callbackUrl
    ) {
        public static PgClientRequest from(PaymentData paymentData, String callbackUrl) {
            return new PgClientRequest(
                    paymentData.orderId().toString(),
                    paymentData.cardType(),
                    paymentData.cardNumber(),
                    paymentData.finalTotalPrice(),
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

    public record PgClientQueryResponse(
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
                    throw new IllegalArgumentException("Transaction Key는 비어있을 수 없습니다.");
                }
            }
        }
    }

    public record PgClientHistoryResponse(
            Meta meta,
            Data data
    ) {
        public record Meta(
                String result,
                String errorCode,
                String message
        ) {}

        public record Data(
                String orderId,
                List<Transaction> transactions
        ) {
            public record Transaction(
                    String transactionKey,
                    String status,
                    String reason
            ) {
                public Transaction {
                    if (transactionKey == null || transactionKey.isBlank()) {
                        throw new IllegalArgumentException("Transaction Key는 비어있을 수 없습니다.");
                    }
                }
            }
        }
    }
}
