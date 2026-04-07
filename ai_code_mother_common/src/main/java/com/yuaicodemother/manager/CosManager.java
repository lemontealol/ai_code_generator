package com.yuaicodemother.manager;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.yuaicodemother.config.CosClientConfig;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * OSS对象存储管理器
 *
 * @author yupi
 */
@Component
//确保cosClientConfig和cosClient都已经被实例化
@ConditionalOnBean(OSS.class)
@Slf4j
public class CosManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private OSS cosClient;

    /**
     * 上传对象
     *
     * @param key  唯一键
     * @param file 文件
     * @return 上传结果
     */
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 上传文件到 COS 并返回访问 URL
     *
     * @param key  COS对象键（完整路径）
     * @param file 要上传的文件
     * @return 文件的访问URL，失败返回null
     */
    public String uploadFile(String key, File file) {
        // 上传文件
        PutObjectResult result = putObject(key, file);
        if (result != null) {
            String bucketUrl = "http://code-mother-web-page-bucket.";
            // 构建访问URL
            String url = String.format(bucketUrl+"%s/%s", cosClientConfig.getEndpoint(), key);
            log.info("文件上传COS成功: {} -> {}", file.getName(), url);
            return  url;
        } else {
            log.error("文件上传COS失败，返回结果为空");
            return null;
        }
    }
}
