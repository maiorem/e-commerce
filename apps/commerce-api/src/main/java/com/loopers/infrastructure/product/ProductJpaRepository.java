package com.loopers.infrastructure.product;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.loopers.domain.product.ProductModel;

import jakarta.persistence.LockModeType;

public interface ProductJpaRepository extends JpaRepository<ProductModel, Long> {

    List<ProductModel> findAllByBrandId(Long brandId);

    List<ProductModel> findAllByCategoryId(Long categoryId);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM ProductModel p WHERE p.id = :id")
    Optional<ProductModel> findByIdForUpdate(@Param("id") Long id);
}
