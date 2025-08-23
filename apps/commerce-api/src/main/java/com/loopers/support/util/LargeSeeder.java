package com.loopers.support.util;

import java.sql.*;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public class LargeSeeder {

    // ---- TARGET COUNTS ----
    public static final int USER_COUNT = 100_000;
    public static final int BRAND_COUNT = 1_000;
    public static final int CATEGORY_COUNT = 500;
    public static final int PRODUCT_COUNT = 1_000_000;
    public static final int LIKE_COUNT = 5_000_000;
    public static final int POINT_COUNT = 50_000;
    public static final int COUPON_COUNT = 10_000;
    public static final int USER_COUPON_COUNT = 50_000; // 유저당 평균 0.5개 쿠폰
    public static final int ORDER_COUNT = 200_000;
    public static final int ORDER_ITEM_COUNT = 600_000;

    // ---- TUNING ----
    public static final int PRODUCT_THREADS = 8;
    public static final int LIKE_THREADS = 8;
    public static final int ORDER_THREADS = 4;
    public static final int BATCH_SIZE = 5_000;

    public static void seedAll(Supplier<Connection> connectionSupplier) {
        long t0 = System.currentTimeMillis();
        
        seedUsers(connectionSupplier, USER_COUNT);
        seedBrands(connectionSupplier, BRAND_COUNT);
        seedCategories(connectionSupplier, CATEGORY_COUNT);
        seedProducts(connectionSupplier, PRODUCT_COUNT, BRAND_COUNT, CATEGORY_COUNT, PRODUCT_THREADS);
        seedLikes(connectionSupplier, LIKE_COUNT, USER_COUNT, PRODUCT_COUNT, LIKE_THREADS);
        seedPoints(connectionSupplier, POINT_COUNT, USER_COUNT);
        seedCoupons(connectionSupplier, COUPON_COUNT);
        seedUserCoupons(connectionSupplier, USER_COUPON_COUNT, USER_COUNT, COUPON_COUNT);
        seedOrders(connectionSupplier, ORDER_COUNT, USER_COUNT, ORDER_THREADS);
        seedOrderItems(connectionSupplier, ORDER_ITEM_COUNT, ORDER_COUNT, PRODUCT_COUNT);
        
        long t1 = System.currentTimeMillis();
        System.out.println("[LargeSeeder] done in " + (t1 - t0) + " ms");
    }

    // ---------- USER (single-thread) ----------
    public static void seedUsers(Supplier<Connection> cs, int count) {
        final String sql = "INSERT INTO user (user_id, email, gender, birth_date, version, created_at, updated_at) VALUES (?,?,?,?,?,?,?)";
        try (Connection conn = cs.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);

            for (int i = 1; i <= count; i++) {
                ps.setString(1, "user" + String.format("%06d", i));
                ps.setString(2, "user" + i + "@loopers.com");
                ps.setString(3, i % 2 == 0 ? "MALE" : "FEMALE");
                ps.setString(4, generateRandomBirthDate());
                ps.setLong(5, 0L); // version 기본값 0

                ZonedDateTime[] ts = randomRangePast(365 * 3);
                ps.setTimestamp(6, Timestamp.from(ts[0].toInstant()));
                ps.setTimestamp(7, Timestamp.from(ts[1].toInstant()));

                ps.addBatch();
                if (i % BATCH_SIZE == 0) {
                    ps.executeBatch();
                    conn.commit();
                    ps.clearBatch();
                }
            }
            ps.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("seedUsers failed", e);
        }
    }

    // ---------- BRAND (single-thread) ----------
    public static void seedBrands(Supplier<Connection> cs, int count) {
        final String sql = "INSERT INTO brand (name, description, version, created_at, updated_at) VALUES (?,?,?,?,?)";
        try (Connection conn = cs.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);

            String[] brandNames = {"삼성", "LG", "애플", "구글", "마이크로소프트", "아마존", "테슬라", "현대", "기아", "포드"};
            String[] brandDescriptions = {"삼성 브랜드", "LG 브랜드", "애플 브랜드", "구글 브랜드", "마이크로소프트 브랜드", 
                                        "아마존 브랜드", "테슬라 브랜드", "현대 브랜드", "기아 브랜드", "포드 브랜드"};

            for (int i = 1; i <= count; i++) {
                int brandIndex = (i - 1) % brandNames.length;
                ps.setString(1, brandNames[brandIndex] + " " + ((i - 1) / brandNames.length + 1));
                ps.setString(2, brandDescriptions[brandIndex] + " " + ((i - 1) / brandNames.length + 1));
                ps.setLong(3, 0L); // version 기본값 0

                ZonedDateTime[] ts = randomRangePast(365 * 3);
                ps.setTimestamp(4, Timestamp.from(ts[0].toInstant()));
                ps.setTimestamp(5, Timestamp.from(ts[1].toInstant()));

                ps.addBatch();
                if (i % BATCH_SIZE == 0) {
                    ps.executeBatch();
                    conn.commit();
                    ps.clearBatch();
                }
            }
            ps.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("seedBrands failed", e);
        }
    }

    // ---------- CATEGORY (single-thread) ----------
    public static void seedCategories(Supplier<Connection> cs, int count) {
        final String sql = "INSERT INTO category (name, description, version, created_at, updated_at) VALUES (?,?,?,?,?)";
        try (Connection conn = cs.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);

            String[] categoryNames = {"전자제품", "의류", "식품", "도서", "스포츠", "뷰티", "가구", "자동차", "게임", "음악"};
            String[] categoryDescriptions = {"전자제품 카테고리", "의류 카테고리", "식품 카테고리", "도서 카테고리", "스포츠 카테고리", 
                                           "뷰티 카테고리", "가구 카테고리", "자동차 카테고리", "게임 카테고리", "음악 카테고리"};

            for (int i = 1; i <= count; i++) {
                int categoryIndex = (i - 1) % categoryNames.length;
                ps.setString(1, categoryNames[categoryIndex] + " " + ((i - 1) / categoryNames.length + 1));
                ps.setString(2, categoryDescriptions[categoryIndex] + " " + ((i - 1) / categoryNames.length + 1));
                ps.setLong(3, 0L); // version 기본값 0

                ZonedDateTime[] ts = randomRangePast(365 * 3);
                ps.setTimestamp(4, Timestamp.from(ts[0].toInstant()));
                ps.setTimestamp(5, Timestamp.from(ts[1].toInstant()));

                ps.addBatch();
                if (i % BATCH_SIZE == 0) {
                    ps.executeBatch();
                    conn.commit();
                    ps.clearBatch();
                }
            }
            ps.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("seedCategories failed", e);
        }
    }

    // ---------- PRODUCT (parallel) ----------
    public static void seedProducts(Supplier<Connection> cs, int count, int brandCount, int categoryCount, int threads) {
        final List<int[]> ranges = splitRange(count, threads);
        ExecutorService es = Executors.newFixedThreadPool(threads);
        List<Future<?>> futures = new ArrayList<>();

        for (int[] r : ranges) {
            futures.add(es.submit(() -> insertProductsRange(cs, r[0], r[1], brandCount, categoryCount)));
        }
        waitAll(es, futures);
    }

    private static void insertProductsRange(Supplier<Connection> cs, int start, int end, int brandCount, int categoryCount) {
        final String sql = "INSERT INTO product (brand_id, category_id, name, description, price, stock, likes_count, version, created_at, updated_at) VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = cs.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);

            for (int i = start; i <= end; i++) {
                ps.setLong(1, ThreadLocalRandom.current().nextInt(1, brandCount + 1));
                ps.setLong(2, ThreadLocalRandom.current().nextInt(1, categoryCount + 1));
                ps.setString(3, "상품 " + i);
                ps.setString(4, "상품 " + i + "에 대한 설명입니다.");
                ps.setInt(5, samplePrice());
                ps.setInt(6, sampleStock());
                ps.setInt(7, sampleLike());
                ps.setLong(8, 0L); // version 기본값 0

                ZonedDateTime[] ts = randomRangePast(365 * 3);
                ps.setTimestamp(9, Timestamp.from(ts[0].toInstant()));
                ps.setTimestamp(10, Timestamp.from(ts[1].toInstant()));

                ps.addBatch();
                if (i % BATCH_SIZE == 0) {
                    ps.executeBatch();
                    conn.commit();
                    ps.clearBatch();
                }
            }
            ps.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("insertProductsRange failed", e);
        }
    }

    // ---------- LIKE (parallel) ----------
    public static void seedLikes(Supplier<Connection> cs, int count, int userCount, int productCount, int threads) {
        final List<int[]> ranges = splitRange(count, threads);
        ExecutorService es = Executors.newFixedThreadPool(threads);
        List<Future<?>> futures = new ArrayList<>();

        for (int[] r : ranges) {
            futures.add(es.submit(() -> insertLikesRange(cs, r[0], r[1], userCount, productCount)));
        }
        waitAll(es, futures);
    }

    private static void insertLikesRange(Supplier<Connection> cs, int start, int end, int userCount, int productCount) {
        final String sql = "INSERT INTO likes (user_id, product_id, version, created_at, updated_at) VALUES (?,?,?,?,?)";
        try (Connection conn = cs.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);

            for (int i = start; i <= end; i++) {
                ps.setString(1, "user" + String.format("%06d", ThreadLocalRandom.current().nextInt(1, userCount + 1)));
                ps.setLong(2, ThreadLocalRandom.current().nextInt(1, productCount + 1));
                ps.setLong(3, 0L); // version 기본값 0

                ZonedDateTime[] ts = randomRangePast(365 * 3);
                ps.setTimestamp(4, Timestamp.from(ts[0].toInstant()));
                ps.setTimestamp(5, Timestamp.from(ts[1].toInstant()));

                ps.addBatch();
                if (i % BATCH_SIZE == 0) {
                    ps.executeBatch();
                    conn.commit();
                    ps.clearBatch();
                }
            }
            ps.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("insertLikesRange failed", e);
        }
    }

    // ---------- POINT (single-thread) ----------
    public static void seedPoints(Supplier<Connection> cs, int count, int userCount) {
        final String sql = "INSERT INTO point (user_id, amount, expired_at, version, created_at, updated_at) VALUES (?,?,?,?,?,?)";
        try (Connection conn = cs.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);

            for (int i = 1; i <= count; i++) {
                ps.setString(1, "user" + String.format("%06d", ThreadLocalRandom.current().nextInt(1, userCount + 1)));
                ps.setInt(2, samplePointAmount());
                
                // 1년 후 만료
                ZonedDateTime expiredAt = ZonedDateTime.now().plusYears(1);
                ps.setTimestamp(3, Timestamp.from(expiredAt.toInstant()));
                ps.setLong(4, 0L); // version 기본값 0

                ZonedDateTime[] ts = randomRangePast(365 * 3);
                ps.setTimestamp(5, Timestamp.from(ts[0].toInstant()));
                ps.setTimestamp(6, Timestamp.from(ts[1].toInstant()));

                ps.addBatch();
                if (i % BATCH_SIZE == 0) {
                    ps.executeBatch();
                    conn.commit();
                    ps.clearBatch();
                }
            }
            ps.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("seedPoints failed", e);
        }
    }

    // ---------- COUPON (single-thread) ----------
    public static void seedCoupons(Supplier<Connection> cs, int count) {
        final String sql = "INSERT INTO coupon (name, coupon_code, type, status, discount_value, minimum_order_amount, maximum_discount_amount, issued_at, valid_until, version, created_at, updated_at) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = cs.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);

            String[] couponTypes = {"FIXED_AMOUNT", "PERCENTAGE"};
            String[] couponStatuses = {"ACTIVE", "INACTIVE"};

            for (int i = 1; i <= count; i++) {
                ps.setString(1, "쿠폰 " + i);
                ps.setString(2, "COUPON" + String.format("%06d", i));
                ps.setString(3, couponTypes[i % couponTypes.length]);
                ps.setString(4, couponStatuses[i % couponStatuses.length]);
                ps.setInt(5, i % 2 == 0 ? 1000 : 10); // 정액: 1000원, 정률: 10%
                ps.setInt(6, 10000); // 최소 주문 금액
                ps.setInt(7, i % 2 == 0 ? 5000 : 5000); // 최대 할인 금액
                
                ZonedDateTime issuedAt = ZonedDateTime.now().minusDays(ThreadLocalRandom.current().nextInt(0, 365));
                ZonedDateTime validUntil = issuedAt.plusDays(365);
                ps.setTimestamp(8, Timestamp.from(issuedAt.toInstant()));
                ps.setTimestamp(9, Timestamp.from(validUntil.toInstant()));
                ps.setLong(10, 0L); // version 기본값 0

                ZonedDateTime[] ts = randomRangePast(365 * 3);
                ps.setTimestamp(11, Timestamp.from(ts[0].toInstant()));
                ps.setTimestamp(12, Timestamp.from(ts[1].toInstant()));

                ps.addBatch();
                if (i % BATCH_SIZE == 0) {
                    ps.executeBatch();
                    conn.commit();
                    ps.clearBatch();
                }
            }
            ps.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("seedCoupons failed", e);
        }
    }

    // ---------- ORDER (parallel) ----------
    public static void seedOrders(Supplier<Connection> cs, int count, int userCount, int threads) {
        final List<int[]> ranges = splitRange(count, threads);
        ExecutorService es = Executors.newFixedThreadPool(threads);
        List<Future<?>> futures = new ArrayList<>();

        for (int[] r : ranges) {
            futures.add(es.submit(() -> insertOrdersRange(cs, r[0], r[1], userCount)));
        }
        waitAll(es, futures);
    }

    private static void insertOrdersRange(Supplier<Connection> cs, int start, int end, int userCount) {
        // 현재 엔티티 구조에 맞게 수정: total_amount는 Money 타입이므로 제거하고 기본 컬럼만 사용
        final String sql = "INSERT INTO orders (user_id, order_date, order_number, status, payment_method, version, created_at, updated_at) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection conn = cs.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);

            String[] orderStatuses = {"CREATED", "CONFIRMED", "CANCELLED"};
            String[] paymentMethods = {"CREDIT_CARD", "POINT"};

            for (int i = start; i <= end; i++) {
                int userId = ThreadLocalRandom.current().nextInt(1, userCount + 1);
                ps.setString(1, "user" + String.format("%06d", userId));
                
                ZonedDateTime[] ts = recentSkewed(365, 1.8);
                ps.setTimestamp(2, Timestamp.from(ts[0].toInstant())); // order_date
                ps.setString(3, "ORDER" + String.format("%08d", i)); // order_number
                ps.setString(4, orderStatuses[ThreadLocalRandom.current().nextInt(orderStatuses.length)]); // status
                ps.setString(5, paymentMethods[ThreadLocalRandom.current().nextInt(paymentMethods.length)]); // payment_method
                ps.setLong(6, 0L); // version 기본값 0
                ps.setTimestamp(7, Timestamp.from(ts[0].toInstant())); // created_at
                ps.setTimestamp(8, Timestamp.from(ts[1].toInstant())); // updated_at

                ps.addBatch();
                if (i % BATCH_SIZE == 0) {
                    ps.executeBatch();
                    conn.commit();
                    ps.clearBatch();
                }
            }
            ps.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("insertOrdersRange failed [" + start + "~" + end + "]", e);
        }
    }

    // ---------- ORDER_ITEM (single-thread) ----------
    public static void seedOrderItems(Supplier<Connection> cs, int count, int orderCount, int productCount) {
        final String sql = "INSERT INTO order_item (order_id, product_id, quantity, price_at_order, version, created_at, updated_at) VALUES (?,?,?,?,?,?,?)";
        try (Connection conn = cs.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);

            for (int i = 1; i <= count; i++) {
                int orderId = ThreadLocalRandom.current().nextInt(1, orderCount + 1);
                int productId = ThreadLocalRandom.current().nextInt(1, productCount + 1);
                ps.setLong(1, orderId);
                ps.setLong(2, productId);
                ps.setInt(3, ThreadLocalRandom.current().nextInt(1, 10));
                ps.setInt(4, samplePrice());
                ps.setLong(5, 0L); // version 기본값 0

                ZonedDateTime[] ts = recentSkewed(365, 1.8);
                ps.setTimestamp(6, Timestamp.from(ts[0].toInstant()));
                ps.setTimestamp(7, Timestamp.from(ts[1].toInstant()));

                ps.addBatch();
                if (i % BATCH_SIZE == 0) {
                    ps.executeBatch();
                    conn.commit();
                    ps.clearBatch();
                }
            }
            ps.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("seedOrderItems failed", e);
        }
    }

    // ---------- USER_COUPON (single-thread) ----------
    public static void seedUserCoupons(Supplier<Connection> cs, int count, int userCount, int couponCount) {
        final String sql = "INSERT INTO user_coupon (user_id, coupon_code, status, issued_at, version, created_at, updated_at) VALUES (?,?,?,?,?,?,?)";
        try (Connection conn = cs.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);

            for (int i = 1; i <= count; i++) {
                // 랜덤 유저 선택
                int userId = ThreadLocalRandom.current().nextInt(1, userCount + 1);
                // 랜덤 쿠폰 선택
                int couponId = ThreadLocalRandom.current().nextInt(1, couponCount + 1);
                
                ps.setString(1, "user" + String.format("%06d", userId));
                ps.setString(2, "COUPON" + String.format("%06d", couponId));
                ps.setString(3, "AVAILABLE"); // 사용 가능한 상태
                
                ZonedDateTime issuedAt = ZonedDateTime.now().minusDays(ThreadLocalRandom.current().nextInt(0, 365));
                ps.setTimestamp(4, Timestamp.from(issuedAt.toInstant()));
                ps.setLong(5, 0L); // version 기본값 0

                ZonedDateTime[] ts = randomRangePast(365 * 3);
                ps.setTimestamp(6, Timestamp.from(ts[0].toInstant()));
                ps.setTimestamp(7, Timestamp.from(ts[1].toInstant()));

                ps.addBatch();
                if (i % BATCH_SIZE == 0) {
                    ps.executeBatch();
                    conn.commit();
                    ps.clearBatch();
                }
            }
            ps.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException("seedUserCoupons failed", e);
        }
    }


    // ---------- UTILITY METHODS ----------
    private static List<int[]> splitRange(int count, int threads) {
        List<int[]> ranges = new ArrayList<>();
        int chunk = count / threads;
        int remainder = count % threads;
        
        int start = 1;
        for (int i = 0; i < threads; i++) {
            int end = start + chunk - 1 + (i < remainder ? 1 : 0);
            ranges.add(new int[]{start, end});
            start = end + 1;
        }
        return ranges;
    }

    private static void waitAll(ExecutorService es, List<Future<?>> futures) {
        try {
            for (Future<?> future : futures) {
                future.get();
            }
        } catch (Exception e) {
            throw new RuntimeException("Thread execution failed", e);
        } finally {
            es.shutdown();
        }
    }

    private static String generateRandomBirthDate() {
        int year = ThreadLocalRandom.current().nextInt(1960, 2005);
        int month = ThreadLocalRandom.current().nextInt(1, 13);
        int day = ThreadLocalRandom.current().nextInt(1, 29);
        return String.format("%04d-%02d-%02d", year, month, day);
    }

    private static int samplePrice() {
        double rand = Math.random();
        int step = 1000;

        if (rand < 0.85) {
            // 85%: 1,000 ~ 50,000
            int min = 1000;
            int max = 50_000;
            return ((int) (Math.random() * ((double) (max - min) / step + 1)) * step) + min;
        } else if (rand < 0.95) {
            // 10%: 51,000 ~ 300,000
            int min = 51_000;
            int max = 300_000;
            return ((int) (Math.random() * ((double) (max - min) / step + 1)) * step) + min;
        } else {
            // 5%: 301,000 ~ 1,000,000
            int min = 301_000;
            int max = 1_000_000;
            return ((int) (Math.random() * ((double) (max - min) / step + 1)) * step) + min;
        }
    }

    private static int sampleStock() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        if (r.nextDouble() < 0.10) return 0; // 10% 품절
        double n = normal01();
        int q = (int) Math.round(30 + 25 * n);
        if (q < 1) q = 1;
        if (q > 200) q = 200;
        return q;
    }

    private static int sampleLike() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        double lambda = 1.0 / 600.0; // 평균 ~600
        double u = Math.max(1e-12, r.nextDouble());
        int x = (int) Math.floor(-Math.log(u) / lambda);
        return Math.min(x, 50_000);
    }

    private static int samplePointAmount() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        double lambda = 1.0 / 10000.0; // 평균 ~10,000
        double u = Math.max(1e-12, r.nextDouble());
        int x = (int) Math.floor(-Math.log(u) / lambda);
        return Math.min(x, 100_000);
    }

    private static int sampleOrderAmount() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        double lambda = 1.0 / 50000.0; // 평균 ~50,000
        double u = Math.max(1e-12, r.nextDouble());
        int x = (int) Math.floor(-Math.log(u) / lambda);
        return Math.min(x, 500_000);
    }

    private static int samplePaymentAmount() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        double lambda = 1.0 / 50000.0; // 평균 ~50,000
        double u = Math.max(1e-12, r.nextDouble());
        int x = (int) Math.floor(-Math.log(u) / lambda);
        return Math.min(x, 500_000);
    }

    private static String samplePaymentMethod() {
        String[] methods = {"CREDIT_CARD", "DEBIT_CARD", "BANK_TRANSFER", "PAYPAL"};
        return methods[ThreadLocalRandom.current().nextInt(methods.length)];
    }

    private static String generateCardNumber() {
        return String.format("%04d-%04d-%04d-%04d",
            ThreadLocalRandom.current().nextInt(1000, 10000),
            ThreadLocalRandom.current().nextInt(1000, 10000),
            ThreadLocalRandom.current().nextInt(1000, 10000),
            ThreadLocalRandom.current().nextInt(1000, 10000));
    }

    private static double normal01() {
        double u = Math.random();
        double v = Math.random();
        return Math.sqrt(-2 * Math.log(u)) * Math.cos(2 * Math.PI * v);
    }

    private static ZonedDateTime[] randomRangePast(int days) {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime past = now.minusDays(ThreadLocalRandom.current().nextInt(0, days));
        ZonedDateTime created = past.minusMinutes(ThreadLocalRandom.current().nextInt(0, 60));
        ZonedDateTime updated = past.plusMinutes(ThreadLocalRandom.current().nextInt(0, 60));
        return new ZonedDateTime[]{created, updated};
    }

    private static ZonedDateTime[] recentSkewed(int days, double skew) {
        ZonedDateTime now = ZonedDateTime.now();
        double rand = Math.pow(Math.random(), skew);
        int daysAgo = (int) (rand * days);
        ZonedDateTime past = now.minusDays(daysAgo);
        
        ZonedDateTime c = past.minusMinutes(ThreadLocalRandom.current().nextInt(0, 60));
        ZonedDateTime u = past.plusMinutes(ThreadLocalRandom.current().nextInt(0, 60));
        return new ZonedDateTime[]{c, u};
    }

    public static void main(String[] args) throws SQLException {
        // MySQL 드라이버 로드
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✅ MySQL Driver loaded successfully");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ MySQL Driver not found: " + e.getMessage());
            return;
        }

        // 환경변수에서 설정 가져오기
        final String host = System.getenv("MYSQL_HOST") != null ? System.getenv("MYSQL_HOST") : "localhost";
        final String port = System.getenv("MYSQL_PORT") != null ? System.getenv("MYSQL_PORT") : "3306";
        final String user = System.getenv("MYSQL_USER") != null ? System.getenv("MYSQL_USER") : "application";
        final String password = System.getenv("MYSQL_PASSWORD") != null ? System.getenv("MYSQL_PASSWORD") : "application";
        final String database = System.getenv("MYSQL_DATABASE") != null ? System.getenv("MYSQL_DATABASE") : "loopers";

        final String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?rewriteBatchedStatements=true&useSSL=false&allowPublicKeyRetrieval=true";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("✅ Connected to DB: " + url);
            System.out.println("🚀 Starting LargeSeeder...");
            System.out.println("📊 Target counts:");
            System.out.println("   - Users: " + USER_COUNT);
            System.out.println("   - Brands: " + BRAND_COUNT);
            System.out.println("   - Categories: " + CATEGORY_COUNT);
            System.out.println("   - Products: " + PRODUCT_COUNT);
            System.out.println("   - Likes: " + LIKE_COUNT);
            System.out.println("   - Points: " + POINT_COUNT);
            System.out.println("   - Coupons: " + COUPON_COUNT);
            System.out.println("   - User Coupons: " + USER_COUPON_COUNT);
            System.out.println("   - Orders: " + ORDER_COUNT);
        }

        LargeSeeder.seedAll(() -> {
            try {
                return DriverManager.getConnection(url, user, password);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
