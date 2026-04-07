package com.yuaicodemother.service;

/**
 * @author 六味lemontea 2026-01-25
 * @version 1.0
 * @description
 */
public interface ScreenshotService {
    /**
     * 截图并上传到 COS
     *
     * @param webUrl   截图的 URL
     * @return 截图的 URL
     */
    String generateAndUploadScreenshot(String webUrl);
}
