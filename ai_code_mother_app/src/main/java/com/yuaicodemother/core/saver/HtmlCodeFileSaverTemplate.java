package com.yuaicodemother.core.saver;

import cn.hutool.core.util.StrUtil;
import com.yuaicodemother.ai.model.HtmlCodeResult;
import com.yuaicodemother.exception.BusinessException;
import com.yuaicodemother.exception.ErrorCode;
import com.yuaicodemother.model.enums.CodeGenTypeEnum;

/**
 * @author 六味lemontea 2026-01-16
 * @version 1.0
 * @description
 */
public class HtmlCodeFileSaverTemplate extends CodeFileSaverTemplate<HtmlCodeResult>{
    /**
     * 验证输入参数
     *
     * @param result 代码结果对象
     */
    @Override
    protected void validateInput(HtmlCodeResult result) {
        super.validateInput(result);
        if (StrUtil.isBlank(result.getHtmlCode())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "HTML代码不能为空");
        }
    }

    /**
     * 保存文件具体实现方法。
     *
     * @param result         代码结果对象
     * @param uniqueFilePath 文件保存的唯一路径
     */
    @Override
    protected void saverFiles(HtmlCodeResult result, String uniqueFilePath) {
        writeToFile(uniqueFilePath, "index.html", result.getHtmlCode());
    }

    /**
     * 获取HTML 代码生成类型枚举
     *
     * @return HTML 代码生成类型枚举
     */
    @Override
    protected CodeGenTypeEnum getCodeType() {
        return CodeGenTypeEnum.HTML;
    }
}
