package com.yuaicodemother.core.saver;

import com.yuaicodemother.ai.model.HtmlCodeResult;
import com.yuaicodemother.ai.model.MultiFileCodeResult;
import com.yuaicodemother.exception.BusinessException;
import com.yuaicodemother.exception.ErrorCode;
import com.yuaicodemother.model.enums.CodeGenTypeEnum;

import java.io.File;

/**
 * @author 六味lemontea 2026-01-16
 * @version 1.0
 * @description
 */
public class CodeFileSaverExecutor {
    private static final CodeFileSaverTemplate<HtmlCodeResult> htmlCodeFileSaverTemplate = new HtmlCodeFileSaverTemplate();
    private static final CodeFileSaverTemplate<MultiFileCodeResult> multiFileCodeFileSaverTemplate = new MultiFileCodeFileSaverTemplate();
    public static File saveCodeResult(Object codeResult, CodeGenTypeEnum codeGenTypeEnum,Long appId) {
        return switch (codeGenTypeEnum) {
            case HTML -> htmlCodeFileSaverTemplate.saveCode((HtmlCodeResult) codeResult,appId);
            case MULTI_FILE -> multiFileCodeFileSaverTemplate.saveCode((MultiFileCodeResult) codeResult,appId);
            default -> throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型");
        };
    }
}
