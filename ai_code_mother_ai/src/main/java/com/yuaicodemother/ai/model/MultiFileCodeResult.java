package com.yuaicodemother.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

/**
 * @author 六味lemontea 2026-01-14
 * @version 1.0
 * @description
 */
@Data
@Description("多文件代码结果")
public class MultiFileCodeResult {
    /**
     * html代码
     */
    @Description("html代码")
    private String htmlCode;
    /**
     * css代码
     */
    @Description("css代码")
    private String cssCode;
    /**
     * js代码
     */
    @Description("js代码")
    private String jsCode;
    /**
     * 描述
     */
    @Description("描述")
    private String description;
}
