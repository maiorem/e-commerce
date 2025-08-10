package com.loopers;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Redis 설정 속성 클래스
 */
@ConfigurationProperties(prefix = "datasource.redis")
public class RedisProperties {
    private int database = 0;
    private RedisNodeInfo master;
    private List<RedisNodeInfo> replicas;

    public RedisProperties() {
    }

    public int getDatabase() {
        return database;
    }

    public void setDatabase(int database) {
        this.database = database;
    }

    public RedisNodeInfo getMaster() {
        return master;
    }

    public void setMaster(RedisNodeInfo master) {
        this.master = master;
    }

    public List<RedisNodeInfo> getReplicas() {
        return replicas;
    }

    public void setReplicas(List<RedisNodeInfo> replicas) {
        this.replicas = replicas;
    }
}
