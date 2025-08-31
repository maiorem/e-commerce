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
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("org.testcontainers:kafka")

    testFixturesImplementation("org.testcontainers:kafka")
}
tasks.test {
    useJUnitPlatform()
}
