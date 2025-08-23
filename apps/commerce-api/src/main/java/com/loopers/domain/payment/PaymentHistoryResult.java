package com.loopers.domain.payment;

import com.loopers.infrastructure.http.PgClientDto;
import lombok.Getter;

import java.util.List;

@Getter
public class PaymentHistoryResult {
    private final boolean querySuccess;
    private final String orderId;
    private final List<Transaction> transactions;
    private final String errorMessage;

    private PaymentHistoryResult(boolean querySuccess, String orderId, List<Transaction> transactions, String errorMessage) {
        this.querySuccess = querySuccess;
        this.orderId = orderId;
        this.transactions = transactions;
        this.errorMessage = errorMessage;
    }

    public static PaymentHistoryResult success(PgClientDto.PgClientHistoryResponse response) {
        List<Transaction> transactions = response.data().transactions().stream()
                .map(t -> new Transaction(t.transactionKey(), t.status(), t.reason()))
                .toList();

        return new PaymentHistoryResult(true, response.data().orderId(), transactions, null);
    }

    public static PaymentHistoryResult failed(String errorMessage) {
        return new PaymentHistoryResult(false, null, List.of(), errorMessage);
    }

    public boolean isQuerySuccess() {
        return querySuccess;
    }

    @Getter
    public static class Transaction {
        private final String transactionKey;
        private final String status;
        private final String reason;

        public Transaction(String transactionKey, String status, String reason) {
            this.transactionKey = transactionKey;
            this.status = status;
            this.reason = reason;
        }
    }
}
