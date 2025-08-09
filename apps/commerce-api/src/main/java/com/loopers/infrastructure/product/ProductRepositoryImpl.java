package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductSortBy;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

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
    public Page<ProductModel> findSearchProductList(Pageable pageable, String productName, Long brandId, Long categoryId, ProductSortBy sortBy) {
        // 필터링
        BooleanBuilder filterBuilder = ProductQueryFilter.createFilterBuilder(productName, brandId, categoryId);

        // 정렬
        OrderSpecifier<?> orderSpecifier = ProductQueryFilter.getOrderSpecifier(sortBy);

        List<ProductModel> list = jpaQueryFactory
                .selectFrom(productModel)
                .where(filterBuilder)
                .orderBy(orderSpecifier)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = jpaQueryFactory
                .selectFrom(productModel)
                .where(filterBuilder)
                .fetch().size();

        return new PageImpl<>(list, pageable, total);
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
