package com.loopers.application.like;

import com.loopers.domain.like.LikeModel;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.user.UserId;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LikeApplicationServiceWithFakeTest {

    @Mock
    private ProductLikeHandler productLikeHandler;

    private LikeApplicationService likeApplicationService;
    private FakeLikeRepository fakeLikeRepository;
    private FakeProductRepository fakeProductRepository;
    
    private UserId userId;
    private ProductModel product;

    @BeforeEach
    void setUp() {
        fakeLikeRepository = new FakeLikeRepository();
        fakeProductRepository = new FakeProductRepository();
        
        likeApplicationService = new LikeApplicationService(
                fakeLikeRepository,
                fakeProductRepository,
                productLikeHandler, // Mock 주입
                null, // BrandRepository
                null  // CategoryRepository
        );
        
        userId = UserId.of("seyoung");
        product = fakeProductRepository.createProduct("테스트 상품", "테스트 상품 설명", 10000, 100);
    }

    @Nested
    @DisplayName("좋아요 등록 시")
    class Like_Product {

        @Test
        @DisplayName("정상적으로 좋아요를 등록할 수 있다.")
        void likeSuccess() {
            // given
            Long productId = product.getId();
            LikeModel like = LikeModel.create(userId, productId);
            when(productLikeHandler.addLike(any(ProductModel.class), eq(userId))).thenReturn(like);

            // when
            likeApplicationService.like(userId, productId);

            // then
            verify(productLikeHandler).addLike(any(ProductModel.class), eq(userId));
            assertThat(fakeLikeRepository.countByProductId(productId)).isEqualTo(1);
            assertThat(fakeLikeRepository.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("중복 좋아요 요청 시 첫 번째만 처리되고 두 번째는 무시된다.")
        void duplicateLikeRequest() {
            // given
            Long productId = product.getId();
            LikeModel like = LikeModel.create(userId, productId);
            
            // 첫 번째 호출에서는 LikeModel 반환, 두 번째 호출에서는 null 반환 (이미 좋아요된 상태)
            when(productLikeHandler.addLike(any(ProductModel.class), eq(userId)))
                    .thenReturn(like)
                    .thenReturn(null);

            // when - 첫 번째 좋아요 요청
            likeApplicationService.like(userId, productId);

            // then - 첫 번째 요청이 성공적으로 처리됨
            verify(productLikeHandler).addLike(any(ProductModel.class), eq(userId));
            assertThat(fakeLikeRepository.countByProductId(productId)).isEqualTo(1);

            // when - 두 번째 좋아요 요청 (중복)
            likeApplicationService.like(userId, productId);

            // then - 두 번째 요청은 무시됨 (상태 변화 없음)
            verify(productLikeHandler, times(2)).addLike(any(ProductModel.class), eq(userId));
            assertThat(fakeLikeRepository.countByProductId(productId)).isEqualTo(1); // 여전히 1개
            assertThat(fakeLikeRepository.size()).isEqualTo(1); // 여전히 1개
        }

        @Test
        @DisplayName("존재하지 않는 상품에 좋아요를 시도하면 예외가 발생한다.")
        void likeNonExistentProduct() {
            // given
            Long nonExistentProductId = 999L;

            // when & then
            assertThatThrownBy(() -> likeApplicationService.like(userId, nonExistentProductId))
                    .isInstanceOf(CoreException.class)
                    .hasFieldOrPropertyWithValue("errorType", ErrorType.NOT_FOUND);

            verify(productLikeHandler, never()).addLike(any(ProductModel.class), any(UserId.class));
            assertThat(fakeLikeRepository.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("좋아요 취소 시")
    class Unlike_Product {

        @Test
        @DisplayName("정상적으로 좋아요를 취소할 수 있다.")
        void unlikeSuccess() {
            // given
            Long productId = product.getId();
            LikeModel existingLike = LikeModel.create(userId, productId);
            when(productLikeHandler.removeLike(any(ProductModel.class), eq(userId))).thenReturn(existingLike);

            // when
            likeApplicationService.unlike(userId, productId);

            // then
            verify(productLikeHandler).removeLike(any(ProductModel.class), eq(userId));
            assertThat(fakeLikeRepository.countByProductId(productId)).isEqualTo(0);
            assertThat(fakeLikeRepository.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("중복 좋아요 취소 요청 시 첫 번째만 처리되고 두 번째는 무시된다.")
        void duplicateUnlikeRequest() {
            // given
            Long productId = product.getId();
            LikeModel existingLike = LikeModel.create(userId, productId);
            
            // 첫 번째 호출에서는 LikeModel 반환, 두 번째 호출에서는 null 반환 (이미 취소된 상태)
            when(productLikeHandler.removeLike(any(ProductModel.class), eq(userId)))
                    .thenReturn(existingLike)
                    .thenReturn(null);

            // when - 첫 번째 좋아요 취소 요청
            likeApplicationService.unlike(userId, productId);

            // then - 첫 번째 요청이 성공적으로 처리됨
            verify(productLikeHandler).removeLike(any(ProductModel.class), eq(userId));
            assertThat(fakeLikeRepository.countByProductId(productId)).isEqualTo(0);

            // when - 두 번째 좋아요 취소 요청 (중복)
            likeApplicationService.unlike(userId, productId);

            // then - 두 번째 요청은 무시됨 (삭제 호출되지 않음)
            verify(productLikeHandler, times(2)).removeLike(any(ProductModel.class), eq(userId));
            assertThat(fakeLikeRepository.countByProductId(productId)).isEqualTo(0); // 여전히 0개
            assertThat(fakeLikeRepository.isEmpty()).isTrue(); // 여전히 비어있음
        }

        @Test
        @DisplayName("존재하지 않는 상품에 좋아요 취소를 시도하면 예외가 발생한다.")
        void unlikeNonExistentProduct() {
            // given
            Long nonExistentProductId = 999L;

            // when & then
            assertThatThrownBy(() -> likeApplicationService.unlike(userId, nonExistentProductId))
                    .isInstanceOf(CoreException.class)
                    .hasFieldOrPropertyWithValue("errorType", ErrorType.NOT_FOUND);

            verify(productLikeHandler, never()).removeLike(any(ProductModel.class), any(UserId.class));
            assertThat(fakeLikeRepository.isEmpty()).isTrue();
        }
    }

    @Nested
    @DisplayName("좋아요 상태 확인 시")
    class Check_Like_Status {

        @Test
        @DisplayName("좋아요를 누른 상품은 true를 반환한다.")
        void isLikedTrue() {
            // given
            Long productId = product.getId();
            when(productLikeHandler.isLiked(productId, userId)).thenReturn(true);

            // when
            boolean result = likeApplicationService.isLiked(userId, productId);

            // then
            assertThat(result).isTrue();
            verify(productLikeHandler).isLiked(productId, userId);
        }

        @Test
        @DisplayName("좋아요를 누르지 않은 상품은 false를 반환한다.")
        void isLikedFalse() {
            // given
            Long productId = product.getId();
            when(productLikeHandler.isLiked(productId, userId)).thenReturn(false);

            // when
            boolean result = likeApplicationService.isLiked(userId, productId);

            // then
            assertThat(result).isFalse();
            verify(productLikeHandler).isLiked(productId, userId);
        }
    }
} 
