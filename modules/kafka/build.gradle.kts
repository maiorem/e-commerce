plugins {
    `java-library`
    `java-test-fixtures`
}

group = "com.loopers"
version = "init"

repositories {
    mavenCentral()
}

dependencies {
    api("org.springframework.kafka:spring-kafka")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.springframework.kafka:spring-kafka")
    testImplementation("org.testcontainers:kafka")

    testFixturesImplementation("org.testcontainers:testcontainers")
    testFixturesImplementation("org.testcontainers:kafka")
    testFixturesImplementation("org.springframework.kafka:spring-kafka")
    testFixturesImplementation("org.springframework:spring-test")
}
tasks.test {
    useJUnitPlatform()
}
