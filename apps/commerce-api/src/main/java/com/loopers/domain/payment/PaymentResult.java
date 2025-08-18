package com.loopers.domain.payment;

import com.loopers.infrastructure.pg_client.PgClientDto;

public record PaymentResult(Result result, String errorCode, String message, String transactionKey, PaymentStatus status, String reason) {

    public static PaymentResult success(PgClientDto.PgClientResponse response) {

        return new PaymentResult(
                Result.valueOf(response.meta().result().toUpperCase()),
                response.meta().errorCode(),
                response.meta().message(),
                response.data().transactionKey(),
                PaymentStatus.valueOf(response.data().status().toUpperCase()),
                response.data().reason()
        );
    }

    public static PaymentResult from(PgClientDto.PgClientResponse response) {
        if (response.meta().result().equalsIgnoreCase("success")) {
            return success(response);
        } else {
            return new PaymentResult(
                    Result.valueOf(response.meta().result().toUpperCase()),
                    response.meta().errorCode(),
                    response.meta().message(),
                    null,
                    PaymentStatus.FAILED,
                    response.data().reason()
            );
        }
    }

    public static PaymentResult from(Result result, String errorCode, String message, String transactionKey, PaymentStatus status, String reason) {
        return new PaymentResult(result, errorCode, message, transactionKey, status, reason);
    }


    public boolean isSuccess() {
        return this.status == PaymentStatus.SUCCESS;
    }

    public boolean isFailed() {
        return this.status == PaymentStatus.FAILED;
    }
}
