-- 주간 랭킹 Materialized View
CREATE TABLE mv_product_rank_weekly (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    week_year INTEGER NOT NULL,
    week_number INTEGER NOT NULL,
    rank_position INTEGER NOT NULL,
    total_score DECIMAL(10,2) NOT NULL,
    view_count BIGINT NOT NULL DEFAULT 0,
    like_count BIGINT NOT NULL DEFAULT 0,
    sales_count BIGINT NOT NULL DEFAULT 0,
    total_sales_amount BIGINT NOT NULL DEFAULT 0,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    
    -- 유니크 (같은 주차에 동일한 상품이 중복 저장되지 않도록)
    UNIQUE KEY uk_weekly_product (week_year, week_number, product_id)
);

-- 주간 랭킹 인덱스
CREATE INDEX idx_weekly_rank_period ON mv_product_rank_weekly (week_year, week_number);
CREATE INDEX idx_weekly_rank_position ON mv_product_rank_weekly (week_year, week_number, rank_position);
CREATE INDEX idx_weekly_rank_score ON mv_product_rank_weekly (total_score DESC);

-- 월간 랭킹 Materialized View
CREATE TABLE mv_product_rank_monthly (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    year INTEGER NOT NULL,
    month INTEGER NOT NULL,
    rank_position INTEGER NOT NULL,
    total_score DECIMAL(10,2) NOT NULL,
    view_count BIGINT NOT NULL DEFAULT 0,
    like_count BIGINT NOT NULL DEFAULT 0,
    sales_count BIGINT NOT NULL DEFAULT 0,
    total_sales_amount BIGINT NOT NULL DEFAULT 0,
    version BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    
    -- 유니크 (같은 월에 동일한 상품이 중복 저장되지 않도록)
    UNIQUE KEY uk_monthly_product (year, month, product_id)
);

-- 월간 랭킹 인덱스
CREATE INDEX idx_monthly_rank_period ON mv_product_rank_monthly (year, month);
CREATE INDEX idx_monthly_rank_position ON mv_product_rank_monthly (year, month, rank_position);
CREATE INDEX idx_monthly_rank_score ON mv_product_rank_monthly (total_score DESC);