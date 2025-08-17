package com.loopers.support.util;

import java.sql.*;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.concurrent.ThreadLocalRandom;

public class LargeSeeder {

    // ---- TARGET COUNTS ----
    public static final int USER_COUNT = 100_000;
    public static final int BRAND_COUNT = 1_000;
    public static final int CATEGORY_COUNT = 500;
    public static final int PRODUCT_COUNT = 1_000_000;
    public static final int LIKE_COUNT = 5_000_000;
    public static final int POINT_COUNT = 50_000;
    public static final int COUPON_COUNT = 10_000;
    public static final int ORDER_COUNT = 200_000;
    public static final int ORDER_ITEM_COUNT = 600_000;
    public static final int PAYMENT_COUNT = 200_000;

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
        seedOrders(connectionSupplier, ORDER_COUNT, USER_COUNT, ORDER_THREADS);
        seedOrderItems(connectionSupplier, ORDER_ITEM_COUNT, ORDER_COUNT, PRODUCT_COUNT);
        seedPayments(connectionSupplier, PAYMENT_COUNT, ORDER_COUNT);
        
        long t1 = System.currentTimeMillis();
        System.out.println("[LargeSeeder] done in " + (t1 - t0) + " ms");
    }

    // ---------- USER (single-thread) ----------
    public static void seedUsers(Supplier<Connection> cs, int count) {
        final String sql = "INSERT INTO user (user_id, email, gender, birth_date, created_at, updated_at) VALUES (?,?,?,?,?,?)";
        try (Connection conn = cs.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);

            for (int i = 1; i <= count; i++) {
                ps.setString(1, "user" + String.format("%06d", i));
                ps.setString(2, "user" + i + "@loopers.com");
                ps.setString(3, i % 2 == 0 ? "MALE" : "FEMALE");
                ps.setString(4, generateRandomBirthDate());

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
            throw new RuntimeException("seedUsers failed", e);
        }
    }

    // ---------- BRAND (single-thread) ----------
    public static void seedBrands(Supplier<Connection> cs, int count) {
        final String sql = "INSERT INTO brand (name, description, created_at, updated_at) VALUES (?,?,?,?)";
        try (Connection conn = cs.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);

            for (int i = 1; i <= count; i++) {
                ps.setString(1, "브랜드 " + i);
                ps.setString(2, "브랜드 " + i + " 설명");

                ZonedDateTime[] ts = randomRangePast(365 * 3);
                ps.setTimestamp(3, Timestamp.from(ts[0].toInstant()));
                ps.setTimestamp(4, Timestamp.from(ts[1].toInstant()));

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
        final String sql = "INSERT INTO category (name, description, created_at, updated_at) VALUES (?,?,?,?)";
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

                ZonedDateTime[] ts = randomRangePast(365 * 3);
                ps.setTimestamp(3, Timestamp.from(ts[0].toInstant()));
                ps.setTimestamp(4, Timestamp.from(ts[1].toInstant()));

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
        final String sql = "INSERT INTO product (brand_id, category_id, name, description, price, stock, likes_count, created_at, updated_at) VALUES (?,?,?,?,?,?,?,?,?)";
        try (Connection conn = cs.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);

            for (int i = start; i <= end; i++) {
                int brandId = skewedBrandId(brandCount, 2.2);
                int categoryId = skewedCategoryId(categoryCount, 1.5);
                ps.setLong(1, brandId);
                ps.setLong(2, categoryId);
                ps.setString(3, "상품 " + i);
                ps.setString(4, "상품 " + i + " 설명");
                ps.setInt(5, samplePrice());
                ps.setInt(6, sampleStock());
                ps.setInt(7, sampleLike());

                ZonedDateTime[] ts = recentSkewed(365, 2.3);
                ps.setTimestamp(8, Timestamp.from(ts[0].toInstant()));
                ps.setTimestamp(9, Timestamp.from(ts[1].toInstant()));

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
            throw new RuntimeException("insertProductsRange failed [" + start + "~" + end + "]", e);
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
        final String sql = "INSERT INTO likes (user_id, product_id, created_at, updated_at) VALUES (?,?,?,?)";
        try (Connection conn = cs.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);

            for (int i = start; i <= end; i++) {
                int userId = ThreadLocalRandom.current().nextInt(1, userCount + 1);
                int productId = skewedProductId(productCount, 2.0);
                ps.setString(1, "user" + String.format("%06d", userId));
                ps.setLong(2, productId);

                ZonedDateTime[] ts = recentSkewed(90, 1.5);
                ps.setTimestamp(3, Timestamp.from(ts[0].toInstant()));
                ps.setTimestamp(4, Timestamp.from(ts[1].toInstant()));

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
            throw new RuntimeException("insertLikesRange failed [" + start + "~" + end + "]", e);
        }
    }

    // ---------- POINT (single-thread) ----------
    public static void seedPoints(Supplier<Connection> cs, int count, int userCount) {
        final String sql = "INSERT INTO point (user_id, amount, expired_at, created_at, updated_at) VALUES (?,?,?,?,?)";
        try (Connection conn = cs.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);

            for (int i = 1; i <= count; i++) {
                int userId = ThreadLocalRandom.current().nextInt(1, userCount + 1);
                ps.setString(1, "user" + String.format("%06d", userId));
                ps.setInt(2, samplePointAmount());
                ps.setTimestamp(3, Timestamp.from(ZonedDateTime.now().plusYears(1).toInstant()));

                ZonedDateTime[] ts = recentSkewed(30, 1.0);
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
            throw new RuntimeException("seedPoints failed", e);
        }
    }

    // ---------- COUPON (single-thread) ----------
    public static void seedCoupons(Supplier<Connection> cs, int count) {
        final String sql = "INSERT INTO coupon (name, coupon_code, type, status, discount_value, minimum_order_amount, maximum_discount_amount, issued_at, valid_until, created_at, updated_at) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = cs.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);

            for (int i = 1; i <= count; i++) {
                ps.setString(1, "쿠폰 " + i);
                ps.setString(2, "COUPON" + String.format("%06d", i));
                ps.setInt(3, i % 2 == 0 ? 0 : 1); // 0: FIXED_AMOUNT, 1: PERCENTAGE
                ps.setInt(4, 1); // 1: ACTIVE
                ps.setInt(5, i % 2 == 0 ? 5000 : 20); // FIXED_AMOUNT: 5000원, PERCENTAGE: 20%
                ps.setInt(6, 10000);
                ps.setInt(7, 10000);
                ps.setTimestamp(8, Timestamp.from(ZonedDateTime.now().toInstant()));
                ps.setTimestamp(9, Timestamp.from(ZonedDateTime.now().plusMonths(6).toInstant()));

                ZonedDateTime[] ts = recentSkewed(30, 1.0);
                ps.setTimestamp(10, Timestamp.from(ts[0].toInstant()));
                ps.setTimestamp(11, Timestamp.from(ts[1].toInstant()));

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
        final String sql = "INSERT INTO orders (user_id, total_amount, order_date, order_number, created_at, updated_at) VALUES (?,?,?,?,?,?)";
        try (Connection conn = cs.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);

            for (int i = start; i <= end; i++) {
                int userId = ThreadLocalRandom.current().nextInt(1, userCount + 1);
                ps.setString(1, "user" + String.format("%06d", userId));
                ps.setInt(2, sampleOrderAmount());
                ps.setString(4, "ORDER" + String.format("%08d", i)); // order_number 추가

                ZonedDateTime[] ts = recentSkewed(365, 1.8);
                ps.setTimestamp(3, Timestamp.from(ts[0].toInstant()));
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
            throw new RuntimeException("insertOrdersRange failed [" + start + "~" + end + "]", e);
        }
    }

    // ---------- ORDER_ITEM (single-thread) ----------
    public static void seedOrderItems(Supplier<Connection> cs, int count, int orderCount, int productCount) {
        final String sql = "INSERT INTO order_item (order_id, product_id, quantity, price_at_order, created_at, updated_at) VALUES (?,?,?,?,?,?)";
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

                ZonedDateTime[] ts = recentSkewed(365, 1.8);
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
            throw new RuntimeException("seedOrderItems failed", e);
        }
    }

    // ---------- PAYMENT (single-thread) ----------
    public static void seedPayments(Supplier<Connection> cs, int count, int orderCount) {
        final String sql = "INSERT INTO payment_history (order_id, payment_method, payment_status, amount, payment_date, created_at, updated_at) VALUES (?,?,?,?,?,?,?)";
        try (Connection conn = cs.get();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);

            for (int i = 1; i <= count; i++) {
                int orderId = ThreadLocalRandom.current().nextInt(1, orderCount + 1);
                ps.setLong(1, orderId);
                ps.setString(2, samplePaymentMethod());
                ps.setString(3, "SUCCESS"); // 기본적으로 성공 상태
                ps.setInt(4, samplePaymentAmount());

                ZonedDateTime[] ts = recentSkewed(365, 1.8);
                ps.setTimestamp(5, Timestamp.from(ts[0].toInstant()));
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
            throw new RuntimeException("seedPayments failed", e);
        }
    }

    // ---------- Helpers ----------

    private static List<int[]> splitRange(int total, int threads) {
        List<int[]> ranges = new ArrayList<>(threads);
        int base = total / threads;
        int rem = total % threads;
        int cur = 1;
        for (int t = 0; t < threads; t++) {
            int size = base + (t < rem ? 1 : 0);
            int start = cur;
            int end = cur + size - 1;
            if (size > 0) ranges.add(new int[]{start, end});
            cur = end + 1;
        }
        return ranges;
    }

    private static void waitAll(ExecutorService es, List<Future<?>> futures) {
        es.shutdown();
        for (Future<?> f : futures) {
            try { f.get(); }
            catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            catch (ExecutionException ee) { throw new RuntimeException(ee.getCause()); }
        }
    }

    private static int skewedBrandId(int brandCount, double alpha) {
        double u = ThreadLocalRandom.current().nextDouble();
        int id = 1 + (int) Math.floor(Math.pow(u, alpha) * brandCount);
        if (id < 1) id = 1;
        if (id > brandCount) id = brandCount;
        return id;
    }

    private static int skewedCategoryId(int categoryCount, double alpha) {
        double u = ThreadLocalRandom.current().nextDouble();
        int id = 1 + (int) Math.floor(Math.pow(u, alpha) * categoryCount);
        if (id < 1) id = 1;
        if (id > categoryCount) id = categoryCount;
        return id;
    }

    private static int skewedProductId(int productCount, double alpha) {
        double u = ThreadLocalRandom.current().nextDouble();
        int id = 1 + (int) Math.floor(Math.pow(u, alpha) * productCount);
        if (id < 1) id = 1;
        if (id > productCount) id = productCount;
        return id;
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

    private static String generateRandomBirthDate() {
        int year = ThreadLocalRandom.current().nextInt(1960, 2010);
        int month = ThreadLocalRandom.current().nextInt(1, 13);
        int day = ThreadLocalRandom.current().nextInt(1, 29);
        return String.format("%04d-%02d-%02d", year, month, day);
    }

    private static double normal01() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        double u1 = Math.max(1e-12, r.nextDouble());
        double u2 = r.nextDouble();
        return Math.sqrt(-2.0 * Math.log(u1)) * Math.cos(2 * Math.PI * u2);
    }

    private static ZonedDateTime[] recentSkewed(int daysRange, double alpha) {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        double u = r.nextDouble();
        int daysBack = (int) Math.floor(Math.pow(u, alpha) * daysRange);
        ZonedDateTime created = ZonedDateTime.now(ZoneId.systemDefault())
                .minusDays(daysBack)
                .minusHours(r.nextInt(0, 24))
                .minusMinutes(r.nextInt(0, 60));
        ZonedDateTime updated = created.plusHours(r.nextInt(0, 240));
        return new ZonedDateTime[]{created, updated};
    }

    private static ZonedDateTime[] randomRangePast(int daysBackMax) {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        int d = r.nextInt(0, daysBackMax + 1);
        ZonedDateTime c = ZonedDateTime.now(ZoneId.systemDefault())
                .minusDays(d)
                .minusHours(r.nextInt(0, 24))
                .minusMinutes(r.nextInt(0, 60));
        ZonedDateTime u = c.plusHours(r.nextInt(0, 240));
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
