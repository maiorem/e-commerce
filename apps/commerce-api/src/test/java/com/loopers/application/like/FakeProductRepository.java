package com.loopers.application.like;

import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.ProductSortBy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class FakeProductRepository implements ProductRepository {
    
    private final Map<Long, ProductModel> products = new ConcurrentHashMap<>();
    private Long nextId = 1L;
    
    @Override
    public Optional<ProductModel> findById(Long id) {
        return Optional.ofNullable(products.get(id));
    }
    
    @Override
    public List<ProductModel> findByBrandId(Long brandId) {
        return products.values().stream()
                .filter(product -> Objects.equals(product.getBrandId(), brandId))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ProductModel> findByCategoryId(Long categoryId) {
        return products.values().stream()
                .filter(product -> Objects.equals(product.getCategoryId(), categoryId))
                .collect(Collectors.toList());
    }
    
    @Override
    public Page<ProductModel> findSearchProductList(Pageable pageable, String productName, Long brandId, Long categoryId, ProductSortBy sortBy) {
        List<ProductModel> filteredProducts = products.values().stream()
                .filter(product -> productName == null || product.getName().contains(productName))
                .filter(product -> brandId == null || Objects.equals(product.getBrandId(), brandId))
                .filter(product -> categoryId == null || Objects.equals(product.getCategoryId(), categoryId))
                .collect(Collectors.toList());
        
        // 정렬 적용
        if (sortBy != null) {
            filteredProducts = sortProducts(filteredProducts, sortBy);
        }
        
        // 페이징 적용
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredProducts.size());
        
        if (start > filteredProducts.size()) {
            return new PageImpl<>(new ArrayList<>(), pageable, filteredProducts.size());
        }
        
        List<ProductModel> pageContent = filteredProducts.subList(start, end);
        return new PageImpl<>(pageContent, pageable, filteredProducts.size());
    }
    
    @Override
    public void save(ProductModel product) {
        if (product.getId() == null) {
            // 새로운 상품인 경우 ID 할당
            Long newId = nextId++;
            // BaseEntity의 id 필드를 reflection으로 설정
            try {
                java.lang.reflect.Field idField = product.getClass().getSuperclass().getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(product, newId);
            } catch (Exception e) {
                throw new RuntimeException("Failed to set ID", e);
            }
        }
        
        // ID가 있으면 업데이트, 없으면 새로 생성
        products.put(product.getId(), product);
    }
    
    @Override
    public List<ProductModel> findAllByIds(List<Long> ids) {
        return ids.stream()
                .map(products::get)
                .filter(Objects::nonNull)
                .toList();
    }
    
    private List<ProductModel> sortProducts(List<ProductModel> products, ProductSortBy sortBy) {
        return switch (sortBy) {
            case LATEST -> products.stream()
                    .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()))
                    .collect(Collectors.toList());
            case LIKES -> products.stream()
                    .sorted((p1, p2) -> Integer.compare(p2.getLikesCount(), p1.getLikesCount()))
                    .collect(Collectors.toList());
            case PRICE_ASC -> products.stream()
                    .sorted((p1, p2) -> Integer.compare(p1.getPrice(), p2.getPrice()))
                    .collect(Collectors.toList());
            case PRICE_DESC -> products.stream()
                    .sorted((p1, p2) -> Integer.compare(p2.getPrice(), p1.getPrice()))
                    .collect(Collectors.toList());
        };
    }
    
    // 테스트용 메서드들
    public void clear() {
        products.clear();
        nextId = 1L;
    }
    
    public int size() {
        return products.size();
    }
    
    public boolean isEmpty() {
        return products.isEmpty();
    }
    
    public ProductModel createProduct(String name, String description, int price, int stock) {
        ProductModel product = ProductModel.builder()
                .brandId(1L)
                .categoryId(1L)
                .name(name)
                .description(description)
                .price(price)
                .stock(stock)
                .likesCount(0)
                .build();
        
        save(product);
        return product;
    }
} 
