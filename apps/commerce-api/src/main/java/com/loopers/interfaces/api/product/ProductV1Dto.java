package com.loopers.interfaces.api.product;

public class ProductV1Dto {

    public record ProductListRequest(
            String productName,
            String brandName,
            String categoryName,
            String sortBy,
            int page,
            int size
    ) {}

}
