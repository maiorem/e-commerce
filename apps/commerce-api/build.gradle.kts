dependencies {
    // add-ons
    implementation(project(":modules:jpa"))
    implementation(project(":modules:redis"))
    implementation(project(":modules:kafka"))
    implementation(project(":supports:jackson"))
    implementation(project(":supports:logging"))
    implementation(project(":supports:monitoring"))

    // web
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${project.properties["springDocOpenApiVersion"]}")

    // querydsl
    implementation("com.querydsl:querydsl-jpa::jakarta")
    annotationProcessor("com.querydsl:querydsl-apt::jakarta")
    annotationProcessor("org.springframework.boot:spring-boot-starter-data-jpa")

    // test-fixtures
    testImplementation(testFixtures(project(":modules:jpa")))
    testImplementation(testFixtures(project(":modules:redis")))

    // retry
    implementation("org.springframework.retry:spring-retry")

    // aspects
    implementation("org.springframework:spring-aspects")

    // Spring Cache
    implementation("org.springframework.boot:spring-boot-starter-cache")

    // Redis Cache
    implementation("org.springframework.boot:spring-boot-starter-data-redis")

    // Feign Client
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign:4.3.0")

    // Resilience4j
    implementation("io.github.resilience4j:resilience4j-spring-boot3")
    implementation("org.springframework.boot:spring-boot-starter-aop")

    // test - Awaitility for async/eventually assertions
    testImplementation("org.awaitility:awaitility:4.2.0")

}

// LargeSeeder 실행을 위한 태스크
tasks.register("runLargeSeeder", JavaExec::class) {
    group = "application"
    description = "Run LargeSeeder to generate test data"
    
    mainClass.set("com.loopers.support.util.LargeSeeder")
    classpath = sourceSets["main"].runtimeClasspath
    
    // JVM 옵션 설정
    jvmArgs = listOf(
        "-Xmx4g",  // 최대 힙 메모리 4GB
        "-Xms2g"   // 초기 힙 메모리 2GB
    )
    
    // 환경변수 설정
    environment("MYSQL_HOST", "localhost")
    environment("MYSQL_PORT", "3306")
    environment("MYSQL_USER", "application")
    environment("MYSQL_PASSWORD", "application")
    environment("MYSQL_DATABASE", "loopers")
}

tasks.withType<org.springframework.boot.gradle.tasks.run.BootRun> {
    jvmArgs = listOf("-Xmx4g", "-XX:+HeapDumpOnOutOfMemoryError", "-XX:HeapDumpPath=./dumps/")
}
