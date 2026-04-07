package com.yuaicodemother.service;

import jakarta.servlet.http.HttpServletResponse;

/**
 * @author 六味lemontea 2026-01-26
 * @version 1.0
 * @description
 */
public interface ProjectDownloadService {
    /**
     * 下载项目压缩包
     *
     * @param projectPath 项目路径
     * @param downloadFilename 下载文件名
     * @param response 响应
     * @return 下载结果
     */
    void downloadProjectAsZip(String projectPath, String downloadFilename, HttpServletResponse response);
}
