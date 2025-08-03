package com.loopers.domain.brand;

import java.util.List;
import java.util.Optional;

public interface BrandRepository {
    
    Optional<BrandModel> findById(Long id);
    
    List<BrandModel> findAll();

    Optional<BrandModel> findByName(String brandName);
}
