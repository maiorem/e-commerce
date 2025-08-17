package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductSortBy;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static com.loopers.domain.product.QProductModel.productModel;

@RequiredArgsConstructor
@Component
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductJpaRepository productJpaRepository;
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Optional<ProductModel> findById(Long id) {
        return productJpaRepository.findById(id);
    }

    @Override
    public Optional<ProductModel> findByIdForUpdate(Long id) {
        return productJpaRepository.findByIdForUpdate(id);
    }

    @Override
    public List<ProductModel> findByBrandId(Long brandId) {
        return productJpaRepository.findAllByBrandId(brandId);
    }

    @Override
    public List<ProductModel> findByCategoryId(Long categoryId) {
        return productJpaRepository.findAllByCategoryId(categoryId);
    }

    @Override
    public List<ProductModel> findSearchProductList(int size, String productName, Long brandId, Long categoryId, ProductSortBy sortBy, Long lastId, Integer lastLikesCount, Integer lastPrice, ZonedDateTime lastCreatedAt) {
        // 필터링
        CursorFilter cursorFilter = CursorFilter.from(lastId, lastLikesCount != null ? lastLikesCount : 0, lastPrice != null ? lastPrice : 0, lastCreatedAt);
        BooleanBuilder filterBuilder = ProductQueryFilter.createFilterBuilder(productName, brandId, categoryId, sortBy, cursorFilter);

        // 정렬
        OrderSpecifier<?>[] orderSpecifier = ProductQueryFilter.getOrderSpecifier(sortBy);

        return jpaQueryFactory
                .selectFrom(productModel)
                .where(filterBuilder)
                .orderBy(orderSpecifier)
                .limit(size)
                .fetch();

    }

    @Override
    public ProductModel save(ProductModel product) {
        return productJpaRepository.save(product);
    }

    @Override
    public List<ProductModel> findAllByIds(List<Long> productIds) {
        return productJpaRepository.findAllById(productIds);
    }

}
