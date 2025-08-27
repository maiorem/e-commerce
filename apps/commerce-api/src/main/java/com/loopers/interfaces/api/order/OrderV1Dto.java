package com.loopers.interfaces.api.order;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.loopers.application.order.OrderCommand;
import com.loopers.application.order.OrderInfo;
import com.loopers.application.order.OrderItemCommand;
import com.loopers.domain.payment.CardType;
import com.loopers.domain.payment.PaymentMethod;
import com.loopers.domain.user.UserId;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import io.swagger.v3.oas.annotations.Parameter;

import java.util.List;

public class OrderV1Dto {

    public record CreateOrderRequest(

            @JsonProperty("user_id")
            @Parameter(description = "사용자 ID", example = "user123")
            String userId,

            @JsonProperty("payment_method")
            @Parameter(description = "결제 방법", example = "CREDIT_CARD")
            PaymentMethod paymentMethod,

            @JsonProperty("card_type")
            @Parameter(description = "카드 타입 (카드 결제시 필수)", example = "SAMSUNG")
            CardType cardType,

            @JsonProperty("card_number")
            @Parameter(description = "카드 번호 (카드 결제시 필수)", example = "1234-5678-9012-3456")
            String cardNumber,

            @JsonProperty("point_amount")
            @Parameter(description = "사용할 포인트 (포인트 결제시 필수)", example = "1000")
            Integer pointAmount,

            @JsonProperty("coupon_code")
            @Parameter(description = "쿠폰 코드 (선택사항)", example = "COUPON123")
            String couponCode,

            @Parameter(description = "주문 상품 목록")
            List<OrderItemRequest> items
    ) {

        public OrderCommand toCommand(String userId) {
            return new OrderCommand(
                    UserId.of(userId),
                    paymentMethod,
                    cardType,
                    cardNumber,
                    couponCode,
                    pointAmount != null ? pointAmount : 0,
                    items.stream().map(OrderItemRequest::toCommand).toList()
            );
        }

        public void validate() {
            if (paymentMethod == PaymentMethod.CREDIT_CARD) {
                if (cardType == null) {
                    throw new CoreException(ErrorType.BAD_REQUEST,"카드 결제시 카드 타입은 필수입니다.");
                }
                if (cardNumber == null || cardNumber.isBlank()) {
                    throw new CoreException(ErrorType.BAD_REQUEST,"카드 결제시 카드 번호는 필수입니다.");
                }
            }

            if (paymentMethod == PaymentMethod.POINT) {
                if (pointAmount == null || pointAmount <= 0) {
                    throw new CoreException(ErrorType.BAD_REQUEST,"포인트 결제시 사용할 포인트는 필수입니다.");
                }
            }

            if (items == null || items.isEmpty()) {
                throw new CoreException(ErrorType.BAD_REQUEST,"주문 상품은 최소 1개 이상이어야 합니다.");
            }
        }
    }

    public record OrderItemRequest(
            @JsonProperty("product_id")
            @Parameter(description = "상품 ID")
            Long productId,

            @JsonProperty("quantity")
            @Parameter(description = "주문 수량")
            int quantity,

            @JsonProperty("product_name")
            @Parameter(description = "상품명")
            String productName,

            @JsonProperty("product_price")
            @Parameter(description = "상품 가격")
            int productPrice
    ) {
        public OrderItemCommand toCommand() {
            return new OrderItemCommand(null, productId, quantity, productName, productPrice);
        }
    }

    public record CreateOrderResponse(
            Long orderId,
            String orderNumber,
            String userId,
            String productName,
            int totalQuantity,
            int totalPrice,
            String orderDate,
            String status
    ) {
        public static CreateOrderResponse from(OrderInfo orderInfo) {
            return new CreateOrderResponse(
                    orderInfo.orderId(),
                    orderInfo.orderNumber().getValue(),
                    orderInfo.userId().getValue(),
                    orderInfo.productName(),
                    orderInfo.quantity(),
                    orderInfo.totalPrice().getAmount(),
                    orderInfo.orderDate(),
                    orderInfo.status().name()
            );
        }
    }
}
