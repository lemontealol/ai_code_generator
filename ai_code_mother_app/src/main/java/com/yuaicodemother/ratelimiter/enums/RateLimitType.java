package com.yuaicodemother.ratelimiter.enums;

/**
 * @author 六味lemontea 2026-02-06
 * @version 1.0
 * @description
 */
public enum RateLimitType {

    /**
     * 接口级别限流
     */
    API,

    /**
     * 用户级别限流
     */
    USER,

    /**
     * IP级别限流
     */
    IP
}
