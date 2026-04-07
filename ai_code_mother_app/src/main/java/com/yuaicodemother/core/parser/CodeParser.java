package com.yuaicodemother.core.parser;

/**
 * @author 六味lemontea 2026-01-15
 * @version 1.0
 * @description
 */
public interface CodeParser<T> {
    /**
     * 解析代码
     * @param codeContent 代码内容
     * @return 解析后的结果
     */
    T parseCode(String codeContent);
}
