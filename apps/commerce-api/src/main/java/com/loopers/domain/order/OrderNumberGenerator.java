package com.loopers.domain.order;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class OrderNumberGenerator {

    private static final String PREFIX = "ORD-";
    private static long counter = 0;

    // ORD_주문날짜_10자리 count
    public static OrderNumber generateOrderNumber() {
        counter++;
        LocalDateTime now = LocalDateTime.now();
        String formattedDate = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return OrderNumber.of(PREFIX + formattedDate + String.format("%010d", counter));
    }

}
