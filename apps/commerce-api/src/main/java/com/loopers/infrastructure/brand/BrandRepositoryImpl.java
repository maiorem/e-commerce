package com.loopers.infrastructure.brand;

import com.loopers.domain.brand.BrandModel;
import com.loopers.domain.brand.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
@Component
public class BrandRepositoryImpl implements BrandRepository {

    private final BrandJpaRepository brandJpaRepository;

    @Override
    public Optional<BrandModel> findById(Long id) {
        return brandJpaRepository.findById(id);
    }

    @Override
    public List<BrandModel> findAll() {
        return brandJpaRepository.findAll();
    }

    @Override
    public Optional<BrandModel> findByName(String brandName) {
        return brandJpaRepository.findByName(brandName);
    }

    @Override
    public List<BrandModel> findAllById(Set<Long> brandIds) {
        return brandJpaRepository.findAllById(brandIds);
    }

}
