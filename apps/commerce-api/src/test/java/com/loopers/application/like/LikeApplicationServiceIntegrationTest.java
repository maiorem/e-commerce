package com.loopers.application.like;

import com.loopers.domain.like.LikeRepository;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.user.UserId;
import com.loopers.infrastructure.product.ProductJpaRepository;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class LikeApplicationServiceIntegrationTest {

    @Autowired
    private LikeApplicationService likeApplicationService;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private ProductJpaRepository productJpaRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    private ProductModel product;
    private UserId userId;

    @BeforeEach
    void setUp() {
        databaseCleanUp.truncateAllTables();
        
        product = ProductModel.builder()
                .brandId(1L)
                .categoryId(1L)
                .name("Apple iPhone 14")
                .description("애플 아이폰 14")
                .price(10000)
                .stock(100)
                .likesCount(0)
                .build();
        productJpaRepository.save(product);
        
        userId = UserId.of("seyoung");
    }

    @Test
    @DisplayName("중복 좋아요 요청 시 첫 번째 요청만 처리되고 두 번째 요청은 무시된다.")
    void duplicateLikeRequest() {
        // given
        Long productId = product.getId();

        // when - 첫 번째 좋아요 요청
        likeApplicationService.like(userId, productId);

        // then - 첫 번째 요청이 성공적으로 처리됨
        assertThat(likeApplicationService.isLiked(userId, productId)).isTrue();
        assertThat(likeRepository.countByProductId(productId)).isEqualTo(1);
        
        ProductModel updatedProduct = productJpaRepository.findById(productId).orElseThrow();
        assertThat(updatedProduct.getLikesCount()).isEqualTo(1);

        // when - 두 번째 좋아요 요청 (중복)
        likeApplicationService.like(userId, productId);

        // then - 두 번째 요청부터 상태 변화 없음
        assertThat(likeApplicationService.isLiked(userId, productId)).isTrue();
        assertThat(likeRepository.countByProductId(productId)).isEqualTo(1);
        
        ProductModel unchangedProduct = productJpaRepository.findById(productId).orElseThrow();
        assertThat(unchangedProduct.getLikesCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("중복 좋아요 취소 요청 시 첫 번째 요청만 처리되고 두 번째 요청은 무시된다.")
    void duplicateUnlikeRequest() {
        // given - 먼저 좋아요를 등록
        Long productId = product.getId();
        likeApplicationService.like(userId, productId);
        assertThat(likeApplicationService.isLiked(userId, productId)).isTrue();

        // when - 첫 번째 좋아요 취소 요청
        likeApplicationService.unlike(userId, productId);

        // then - 첫 번째 요청이 성공적으로 처리됨
        assertThat(likeApplicationService.isLiked(userId, productId)).isFalse();
        assertThat(likeRepository.countByProductId(productId)).isEqualTo(0);
        
        ProductModel updatedProduct = productJpaRepository.findById(productId).orElseThrow();
        assertThat(updatedProduct.getLikesCount()).isEqualTo(0);

        // when - 두 번째 좋아요 취소 요청 (중복)
        likeApplicationService.unlike(userId, productId);

        // then - 두 번째 요청은 무시됨 (상태 변화 없음)
        assertThat(likeApplicationService.isLiked(userId, productId)).isFalse();
        assertThat(likeRepository.countByProductId(productId)).isEqualTo(0);
        
        ProductModel unchangedProduct = productJpaRepository.findById(productId).orElseThrow();
        assertThat(unchangedProduct.getLikesCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("좋아요 등록 후 취소, 다시 등록이 정상적으로 동작한다.")
    void likeUnlikeLikeSequence() {
        // given
        Long productId = product.getId();

        // when - 좋아요 등록
        likeApplicationService.like(userId, productId);

        // then - 좋아요 등록 확인
        assertThat(likeApplicationService.isLiked(userId, productId)).isTrue();
        assertThat(likeRepository.countByProductId(productId)).isEqualTo(1);
        
        ProductModel likedProduct = productJpaRepository.findById(productId).orElseThrow();
        assertThat(likedProduct.getLikesCount()).isEqualTo(1);

        // when - 좋아요 취소
        likeApplicationService.unlike(userId, productId);

        // then - 좋아요 취소 확인
        assertThat(likeApplicationService.isLiked(userId, productId)).isFalse();
        assertThat(likeRepository.countByProductId(productId)).isEqualTo(0);
        
        ProductModel unlikedProduct = productJpaRepository.findById(productId).orElseThrow();
        assertThat(unlikedProduct.getLikesCount()).isEqualTo(0);

        // when - 다시 좋아요 등록
        likeApplicationService.like(userId, productId);

        // then - 다시 좋아요 등록 확인
        assertThat(likeApplicationService.isLiked(userId, productId)).isTrue();
        assertThat(likeRepository.countByProductId(productId)).isEqualTo(1);
        
        ProductModel relikedProduct = productJpaRepository.findById(productId).orElseThrow();
        assertThat(relikedProduct.getLikesCount()).isEqualTo(1);
    }
} 
