package com.yuaicodemother.core.saver;

import com.yuaicodemother.ai.model.MultiFileCodeResult;
import com.yuaicodemother.model.enums.CodeGenTypeEnum;

/**
 * @author 六味lemontea 2026-01-16
 * @version 1.0
 * @description
 */
public class MultiFileCodeFileSaverTemplate extends CodeFileSaverTemplate<MultiFileCodeResult>{
    /**
     * 保存文件
     *
     * @param result         代码结果对象
     * @param uniqueFilePath 文件保存的唯一路径
     */
    @Override
    protected void saverFiles(MultiFileCodeResult result, String uniqueFilePath) {
        writeToFile(uniqueFilePath, "index.html", result.getHtmlCode());
        writeToFile(uniqueFilePath, "style.css", result.getCssCode());
        writeToFile(uniqueFilePath, "script.js", result.getJsCode());
    }

    /**
     * 获取代码生成类型
     *
     * @return 代码生成类型枚举
     */
    @Override
    protected CodeGenTypeEnum getCodeType() {
        return CodeGenTypeEnum.MULTI_FILE;
    }
}
