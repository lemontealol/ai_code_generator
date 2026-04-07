package com.yuaicodemother.core.parser;

import com.yuaicodemother.ai.model.HtmlCodeResult;
import com.yuaicodemother.ai.model.MultiFileCodeResult;
import com.yuaicodemother.exception.BusinessException;
import com.yuaicodemother.exception.ErrorCode;
import com.yuaicodemother.model.enums.CodeGenTypeEnum;

/**
 * @author 六味lemontea 2026-01-16
 * @version 1.0
 * @description
 */
public class CodeParserExecutor {
    private static final CodeParser<HtmlCodeResult> htmlCodeParser = new HtmlCodeParser();
    private static final CodeParser<MultiFileCodeResult> multiFileCodeParser = new MultiFileCodeParser();
    /**
     * 根据代码生成类型执行相应的解析器
     * @param codeContent 代码内容
     * @param codeGenTypeEnum 代码生成类型
     * @return 解析结果
     */
    public  static Object executeParser(String codeContent, CodeGenTypeEnum codeGenTypeEnum) {
       return  switch (codeGenTypeEnum) {
            case HTML -> htmlCodeParser.parseCode(codeContent);
            case MULTI_FILE -> multiFileCodeParser.parseCode(codeContent);
            default -> throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的代码生成类型");
        };
    }
}
