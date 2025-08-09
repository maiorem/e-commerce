package com.loopers.domain.product;

import com.loopers.domain.like.ProductLikeDomainService;
import com.loopers.domain.like.LikeModel;
import com.loopers.domain.like.LikeRepository;
import com.loopers.domain.user.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductLikeDomainServiceTest {

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private ProductModel product;

    @InjectMocks
    private ProductLikeDomainService productLikeDomainService;

    private UserId userId;
    private Long productId;

    @BeforeEach
    void setUp() {
        userId = UserId.of("seyoung");
        productId = 1L;
    }

    @Nested
    @DisplayName("좋아요 추가 시")
    class Add_Like {

        @Test
        @DisplayName("정상적으로 좋아요를 추가할 수 있다.")
        void addLikeSuccess() {
            // given
            when(product.getId()).thenReturn(productId);
            when(likeRepository.existsByUserIdAndProductId(userId, productId)).thenReturn(false);

            // when
            LikeModel result = productLikeDomainService.addLike(product, userId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(userId);
            assertThat(result.getProductId()).isEqualTo(productId);
            verify(product).incrementLikesCount();
            verify(likeRepository).existsByUserIdAndProductId(userId, productId);
        }

        @Test
        @DisplayName("이미 좋아요를 누른 상품에 다시 좋아요를 누르면 null을 반환한다.")
        void addLikeAlreadyLiked() {
            // given
            when(product.getId()).thenReturn(productId);
            when(likeRepository.existsByUserIdAndProductId(userId, productId)).thenReturn(true);

            // when
            LikeModel result = productLikeDomainService.addLike(product, userId);

            // then
            assertThat(result).isNull();
            verify(product, never()).incrementLikesCount();
            verify(likeRepository).existsByUserIdAndProductId(userId, productId);
            verifyNoMoreInteractions(likeRepository);
        }
    }

    @Nested
    @DisplayName("좋아요 제거 시")
    class Remove_Like {

        @Test
        @DisplayName("정상적으로 좋아요를 제거할 수 있다.")
        void removeLikeSuccess() {
            // given
            when(product.getId()).thenReturn(productId);
            LikeModel existingLike = LikeModel.create(userId, productId);
            when(likeRepository.existsByUserIdAndProductId(userId, productId)).thenReturn(true);
            when(likeRepository.findByUserIdAndProductId(userId, productId)).thenReturn(Optional.of(existingLike));

            // when
            LikeModel result = productLikeDomainService.removeLike(product, userId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getUserId()).isEqualTo(userId);
            assertThat(result.getProductId()).isEqualTo(productId);
            verify(product).decrementLikesCount();
            verify(likeRepository).existsByUserIdAndProductId(userId, productId);
            verify(likeRepository).findByUserIdAndProductId(userId, productId);
        }

        @Test
        @DisplayName("좋아요를 누르지 않은 상품의 좋아요를 제거하려 하면 null을 반환한다.")
        void removeLikeNotLiked() {
            // given
            when(product.getId()).thenReturn(productId);
            when(likeRepository.existsByUserIdAndProductId(userId, productId)).thenReturn(false);

            // when
            LikeModel result = productLikeDomainService.removeLike(product, userId);

            // then
            assertThat(result).isNull();
            verify(product, never()).decrementLikesCount();
            verify(likeRepository).existsByUserIdAndProductId(userId, productId);
            verifyNoMoreInteractions(likeRepository);
        }
    }

    @Nested
    @DisplayName("좋아요 상태 확인 시")
    class Check_Like_Status {

        @Test
        @DisplayName("좋아요를 누른 상품은 true를 반환한다.")
        void isLikedTrue() {
            // given
            when(likeRepository.existsByUserIdAndProductId(userId, productId)).thenReturn(true);

            // when
            boolean result = productLikeDomainService.isLiked(productId, userId);

            // then
            assertThat(result).isTrue();
            verify(likeRepository).existsByUserIdAndProductId(userId, productId);
        }

        @Test
        @DisplayName("좋아요를 누르지 않은 상품은 false를 반환한다.")
        void isLikedFalse() {
            // given
            when(likeRepository.existsByUserIdAndProductId(userId, productId)).thenReturn(false);

            // when
            boolean result = productLikeDomainService.isLiked(productId, userId);

            // then
            assertThat(result).isFalse();
            verify(likeRepository).existsByUserIdAndProductId(userId, productId);
        }
    }

    @Nested
    @DisplayName("좋아요 수 조회 시")
    class Get_Like_Count {

        @Test
        @DisplayName("상품의 좋아요 수를 정확히 조회할 수 있다.")
        void getLikeCount() {
            // given
            int expectedCount = 5;
            when(likeRepository.countByProductId(productId)).thenReturn(expectedCount);

            // when
            int result = productLikeDomainService.getLikeCount(productId);

            // then
            assertThat(result).isEqualTo(expectedCount);
            verify(likeRepository).countByProductId(productId);
        }
    }

    @Nested
    @DisplayName("사용자 좋아요 상품 목록 조회 시")
    class Get_Liked_Product_Ids {

        @Test
        @DisplayName("사용자가 좋아요한 상품 ID 목록을 조회할 수 있다.")
        void getLikedProductIds() {
            // given
            List<LikeModel> likes = List.of(
                    LikeModel.create(userId, 1L),
                    LikeModel.create(userId, 2L),
                    LikeModel.create(userId, 3L)
            );
            when(likeRepository.findByUserId(userId)).thenReturn(likes);

            // when
            List<Long> result = productLikeDomainService.getLikedProductIds(userId);

            // then
            assertThat(result).hasSize(3);
            assertThat(result).containsExactly(1L, 2L, 3L);
            verify(likeRepository).findByUserId(userId);
        }

        @Test
        @DisplayName("좋아요한 상품이 없으면 빈 목록을 반환한다.")
        void getLikedProductIdsEmpty() {
            // given
            when(likeRepository.findByUserId(userId)).thenReturn(List.of());

            // when
            List<Long> result = productLikeDomainService.getLikedProductIds(userId);

            // then
            assertThat(result).isEmpty();
            verify(likeRepository).findByUserId(userId);
        }
    }
} 
