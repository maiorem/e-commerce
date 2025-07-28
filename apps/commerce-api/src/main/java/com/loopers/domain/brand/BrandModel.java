package com.loopers.domain.brand;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name = "brand")
@Getter
public class BrandModel extends BaseEntity {

    private String name;
    private String description;

    protected BrandModel() {}

    public static BrandModel of(String name, String description) {
        BrandModel brand = new BrandModel();
        brand.name = name;
        brand.description = description;
        return brand;
    }
} 
