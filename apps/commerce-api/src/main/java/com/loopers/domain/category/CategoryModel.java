package com.loopers.domain.category;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name = "category")
@Getter
public class CategoryModel extends BaseEntity {

    private String name;
    private String description;

    protected CategoryModel() {}

    public static CategoryModel of(String name, String description) {
        CategoryModel category = new CategoryModel();
        category.name = name;
        category.description = description;
        return category;
    }
} 
