package com.loopers.domain.product;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class ProductStockDomainServiceTest {

    @InjectMocks
    private ProductStockDomainService productStockDomainService;

    private ProductModel product;

    @BeforeEach
    void setUp() {
        product = ProductModel.builder()
                .brandId(1L)
                .categoryId(1L)
                .name("테스트 상품")
                .description("테스트 상품 설명")
                .price(10000)
                .stock(100)
                .likesCount(0)
                .build();
    }

    @Nested
    @DisplayName("재고 차감 검증 시")
    class Validate_Stock_Deduction {

        @Test
        @DisplayName("차감할 수량이 0 이하면 예외가 발생한다.")
        void validateStockDeductionZeroQuantity() {
            // when & then
            assertThatThrownBy(() -> productStockDomainService.validateStockDeduction(product, 0))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("차감할 수량이 음수이면 예외가 발생한다.")
        void validateStockDeductionNegativeQuantity() {
            // when & then
            assertThatThrownBy(() -> productStockDomainService.validateStockDeduction(product, -10))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("재고보다 많은 수량을 차감하려 하면 예외가 발생한다.")
        void validateStockDeductionInsufficientStock() {
            // when & then
            assertThatThrownBy(() -> productStockDomainService.validateStockDeduction(product, 150))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("재고 차감 시")
    class Deduct_Stock {

        @Test
        @DisplayName("정상적으로 재고를 차감할 수 있다.")
        void deductStockSuccess() {
            // given
            int originalStock = product.getStock();
            int deductQuantity = 30;

            // when
            productStockDomainService.deductStock(product, deductQuantity);

            // then
            assertThat(product.getStock()).isEqualTo(originalStock - deductQuantity);
        }

        @Test
        @DisplayName("재고 차감 시 검증 로직이 실행된다.")
        void deductStockValidation() {
            // when & then
            assertThatThrownBy(() -> productStockDomainService.deductStock(product, 150))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
