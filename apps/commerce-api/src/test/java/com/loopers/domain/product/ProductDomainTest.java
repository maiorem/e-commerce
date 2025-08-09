package com.loopers.domain.product;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class ProductDomainTest {


    @Nested
    @DisplayName("상품 좋아요 등록 시,")
    class  Product_Like_Register {

        @Test
        @DisplayName("상품의 좋아요 수가 증가한다.")
        void 상품_좋아요를_누르면_좋아요_수가_증가한다() {
            // given
            ProductModel product = ProductModel.builder()
                    .brandId(1L)
                    .categoryId(1L)
                    .name("Nike Air Max")
                    .description("나이키 에어맥스 운동화")
                    .price(10000)
                    .stock(50)
                    .build();
            int initialLikes = product.getLikesCount();

            // when
            product.incrementLikesCount();

            // then
            assertThat(product.getLikesCount()).isEqualTo(initialLikes + 1);
        }

    }

    @Nested
    @DisplayName("상품 좋아요 등록 취소 시,")
    class  Product_CANCEL_LIKE_REGISTER {

        @Test
        @DisplayName("상품의 좋아요 수가 감소한다.")
        void 상품_좋아요를_취소하면_좋아요_수가_감소한다() {
            // given
            ProductModel product = ProductModel.builder()
                    .brandId(1L)
                    .categoryId(1L)
                    .name("Nike Air Max")
                    .description("나이키 에어맥스 운동화")
                    .price(10000)
                    .stock(50)
                    .likesCount(5)
                    .build();
            int initialLikes = product.getLikesCount();

            // when
            product.decrementLikesCount();

            // then
            assertThat(product.getLikesCount()).isEqualTo(initialLikes - 1);
        }

        @Test
        @DisplayName("상품의 좋아요 수가 0보다 작아지면 예외가 발생한다.")
        void 상품_좋아요_수가_0보다_작아지면_예외가_발생한다() {
            // given
            ProductModel product = ProductModel.builder()
                    .brandId(1L)
                    .categoryId(1L)
                    .name("Nike Air Max")
                    .description("나이키 에어맥스 운동화")
                    .price(10000)
                    .stock(50)
                    .likesCount(0)
                    .build();

            // when & then
            assertThatThrownBy(product::decrementLikesCount)
                .isInstanceOf(IllegalArgumentException.class);
        }

    }

    @Nested
    @DisplayName("상품 재고 차감 시,")
    class Product_Stock_Decrease {

        @Test
        @DisplayName("재고가 충분할 경우 재고가 차감된다.")
        void 재고가_충분할_경우_재고가_차감된다() {
            // given
            ProductModel product = ProductModel.builder()
                    .brandId(1L)
                    .categoryId(1L)
                    .name("Nike Air Max")
                    .description("나이키 에어맥스 운동화")
                    .price(10000)
                    .stock(50)
                    .build();
            int initialStock = product.getStock();

            // when
            product.deductStock(10);

            // then
            assertThat(product.getStock()).isEqualTo(initialStock - 10);
        }

        @Test
        @DisplayName("재고가 부족할 경우 예외가 발생한다.")
        void 재고가_부족할_경우_예외가_발생한다() {
            // given
            ProductModel product = ProductModel.builder()
                    .brandId(1L)
                    .categoryId(1L)
                    .name("Nike Air Max")
                    .description("나이키 에어맥스 운동화")
                    .price(10000)
                    .stock(5)
                    .build();

            // when & then
            assertThatThrownBy(() -> product.deductStock(10))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

}
