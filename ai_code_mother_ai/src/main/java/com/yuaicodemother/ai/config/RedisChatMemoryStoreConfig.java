package com.yuaicodemother.ai.config;

import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 六味lemontea 2026-01-21
 * @version 1.0
 * @description redis对话记忆存储
 */
@Configuration
/*yml的redis路径*/
@ConfigurationProperties(prefix = "spring.data.redis")
@Data
public class RedisChatMemoryStoreConfig {
    private String host;
    private int port;
    private String username;
    private String password;
//    存活时间。
    private Long ttl;
    @Bean
    public RedisChatMemoryStore redisChatMemoryStore() {
        return RedisChatMemoryStore.builder()
                .host(host)
                .port( port)
//                .user(username)
                .password( password)
                .ttl(ttl)
                .build();
    }
}
