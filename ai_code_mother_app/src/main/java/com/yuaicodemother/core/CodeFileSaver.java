package com.yuaicodemother.core;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.yuaicodemother.ai.model.HtmlCodeResult;
import com.yuaicodemother.ai.model.MultiFileCodeResult;
import com.yuaicodemother.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * @author 六味lemontea 2026-01-14
 * @version 1.0
 * &#064;description  文件保存器
 */
@Deprecated
public class CodeFileSaver {
    // 文件保存的根目录
    private static final String FILE_SAVE_ROOT_DIR = System.getProperty("user.dir")  + File.separator + "tmp" + File.separator + "code_output";
    /**
     * 保存 HTML 网页代码
     * @param htmlCodeResult    Ai返回的html代码结果。
     * @return 保存的文件对象。
     */
    public static File saveHtmlCodeResult(HtmlCodeResult htmlCodeResult){
        String uniqueFilePath = buildUniqueFilePath(CodeGenTypeEnum.HTML.getValue());
        saveFile(uniqueFilePath, "index.html", htmlCodeResult.getHtmlCode());
        return new File(uniqueFilePath);
    }

    /**
     * 保存多文件代码
     * @param multiFileCodeResult Ai返回的多文件代码结果。
     * @return file 保存的文件对象
      */
    public static File saveMultiFileCodeResult(MultiFileCodeResult multiFileCodeResult){
        String uniqueFilePath = buildUniqueFilePath(CodeGenTypeEnum.MULTI_FILE.getValue());
        saveFile(uniqueFilePath, "index.html", multiFileCodeResult.getHtmlCode());
        saveFile(uniqueFilePath, "style.css", multiFileCodeResult.getCssCode());
        saveFile(uniqueFilePath, "script.js", multiFileCodeResult.getJsCode());
        return new File(uniqueFilePath);
    }
    /** 构建文件的唯一路径 （tmp/code_output/bizType_雪花 ID + 雪花 ID）
     *
     * @param bizType 代码生成类型。
     * @return 构建的文件全路径
     */
    private static String buildUniqueFilePath(String bizType){
        String uniqueFilepath = StrUtil.format("{}_{}", bizType,IdUtil.getSnowflakeNextIdStr()) ;
        String dirPath =  FILE_SAVE_ROOT_DIR +  File.separator + uniqueFilepath;
        FileUtil.mkdir(dirPath);
        return dirPath;

    }
    /**
     * 保存单个文件。
     * @param dirPath
     * @param fileName
     * @param content
     */
    public static void saveFile(String dirPath, String fileName, String content){
        String filePath = dirPath + File.separator + fileName;
        FileUtil.writeString(content, filePath, StandardCharsets.UTF_8 );
    }
}
