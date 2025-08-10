package com.loopers;

/**
 * Redis 노드 정보를 담는 클래스
 */
public class RedisNodeInfo {
    private String host;
    private int port;

    public RedisNodeInfo() {
    }

    public RedisNodeInfo(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
