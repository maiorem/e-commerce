package com.loopers.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "batch")
public class BatchConfigProperties {
    private int chunkSize = 250;
    private int pageSize = 250;
    private int skipLimit = 10;
    private int maxRankSize = 100;

}
