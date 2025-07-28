package com.loopers.infrastructure.product;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.loopers.domain.product.ProductModel;

public interface ProductJpaRepository extends JpaRepository<ProductModel, Long> {

    List<ProductModel> findAllByBrandId(Long brandId);

    List<ProductModel> findAllByCategoryId(Long categoryId);
}
