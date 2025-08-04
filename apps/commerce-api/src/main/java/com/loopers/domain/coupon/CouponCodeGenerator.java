package com.loopers.domain.coupon;

public class CouponCodeGenerator {

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 10;

    public static String generateCouponCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = (int) (Math.random() * ALPHABET.length());
            code.append(ALPHABET.charAt(index));
        }
        return code.toString();
    }

}
