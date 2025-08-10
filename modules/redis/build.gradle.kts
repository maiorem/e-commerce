plugins {
    id("java")
    `java-test-fixtures`
}

group = "com.loopers"
version = "init"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    // Redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter")
    testImplementation("com.redis:testcontainers-redis")
}

// testFixtures에서 Spring과 Testcontainers 의존성 사용 가능하도록 설정
dependencies {
    testFixturesImplementation("org.springframework.boot:spring-boot-starter-data-redis")
    testFixturesImplementation("org.springframework.boot:spring-boot-starter")
    testFixturesImplementation("org.testcontainers:testcontainers")
    testFixturesImplementation("org.testcontainers:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}
