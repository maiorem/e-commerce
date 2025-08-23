package com.loopers.domain.payment;

public record PaymentResult(
        boolean requestSuccess,
        String transactionKey,
        String message,
        RequestResultStatus status
) {
    public static PaymentResult success(String transactionKey) {
        return new PaymentResult(
                true,
                transactionKey,
                "결제 요청이 접수되었습니다",
                RequestResultStatus.PENDING
        );
    }

    public static PaymentResult failed(String message) {
        return new PaymentResult(
                false,
                null,
                message,
                RequestResultStatus.REQUEST_FAILED
        );
    }

    public boolean isSuccess() {
        return this.requestSuccess;
    }

    public boolean isFailed() {
        return !this.requestSuccess;
    }

}
