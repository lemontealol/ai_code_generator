package com.yuaicodemother.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 腾讯云COS配置类
 *
 */
@Configuration
@ConfigurationProperties(prefix = "aliyun.oss")
//配置文件中有host，secretId，secretKey，region，bucket这些字段后面才会生效
@ConditionalOnProperty(
        prefix = "aliyun.oss",
        name = {"endpoint", "accessKeyId", "accessKeySecret", "region", "bucket"}
)
@Data
public class CosClientConfig {

    /**
     * 域名
     */
    private String endpoint;

    /**
     * accessKeyId
     */
    private String accessKeyId;

    /**
     * 密钥（注意不要泄露）
     */
    private String accessKeySecret;

    /**
     * 区域
     */
    private String region;

    /**
     * 桶名
     */
    private String bucket;

    @Bean
    public OSS cosClient() {
        // 初始化用户身份信息(secretId, secretKey)
        DefaultCredentialProvider credentialsProvider = CredentialsProviderFactory.newDefaultCredentialProvider(accessKeyId, accessKeySecret);
        // 设置bucket的区域, COS地域的简称请参照 https://help.aliyun.com/zh/oss/user-guide/oss-sdk-quick-start?spm=a2c4g.11186623.0.0.1a765d1bbDQWYQ
        return OSSClientBuilder.create()
                .endpoint(endpoint)
                .credentialsProvider(credentialsProvider)
                .region(region)
                .build();
    }
}
