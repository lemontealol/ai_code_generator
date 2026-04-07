package com.yuaicodemother.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

/**
 * @author 六味lemontea 2026-01-14
 * @version 1.0
 * @description
 */
@Data
@Description("生成html代码文件的结果")
public class HtmlCodeResult {
    /**
     * html代码
     */
    @Description("html代码")
    private String htmlCode;
    /**
     * 描述
     */
    @Description("生成代码的描述")
    private String description;
}
