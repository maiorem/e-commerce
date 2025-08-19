package com.loopers.domain.payment;

public record PaymentQueryResult(
        String transactionKey,
        String orderId,
        PaymentStatus status,
        String reason,
        boolean querySuccess

    ) {
        public static PaymentQueryResult success(String transactionKey, String orderId, PaymentStatus status, String reason) {
            return new PaymentQueryResult(transactionKey, orderId, status, reason, true);
        }

        public static PaymentQueryResult failed(String error) {
            return new PaymentQueryResult(null, null, null, error, false);
        }

        public boolean isQuerySuccess() {
            return querySuccess;
        }

    }
