package com.loopers.domain.user.event;

public enum UserActionType {
    PRODUCT_VIEW("상품 조회"),
    PRODUCT_CLICK("상품 클릭"),
    PRODUCT_LIKE("상품 좋아요"),
    PRODUCT_UNLIKE("상품 좋아요 취소"),
    ORDER_CREATE("주문 생성"),
    ORDER_CANCEL("주문 취소");

    private final String description;

    UserActionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
