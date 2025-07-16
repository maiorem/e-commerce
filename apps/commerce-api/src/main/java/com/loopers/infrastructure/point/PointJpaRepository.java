package com.loopers.infrastructure.point;

import org.springframework.data.jpa.repository.JpaRepository;

import com.loopers.domain.point.PointModel;

public interface PointJpaRepository extends JpaRepository<PointModel, Long>{

}
