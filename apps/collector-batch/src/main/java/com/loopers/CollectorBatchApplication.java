package com.loopers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CollectorBatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(CollectorBatchApplication.class, args);
    }
}