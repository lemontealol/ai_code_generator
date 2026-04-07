package com.yuaicodemother.core.saver;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.yuaicodemother.exception.BusinessException;
import com.yuaicodemother.exception.ErrorCode;
import com.yuaicodemother.exception.ThrowUtils;
import com.yuaicodemother.model.enums.CodeGenTypeEnum;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * @author 六味lemontea 2026-01-16
 * @version 1.0
 * @description
 */
public abstract class CodeFileSaverTemplate<T> {
    /**
     *  文件保存的根目录
     */
    private static final String FILE_SAVE_ROOT_DIR = System.getProperty("user.dir")  + File.separator + "tmp" + File.separator + "code_output";

    /**
     * 模板方法：给外部调用的保存代码。
     * @param result 代码结果对象
     * @return 文件目录对象
     */
    public final File saveCode(T result,Long appId){
        //1. 验证输入
        validateInput(result);
        //2.构建唯一目录
        String uniqueFilePath = buildUniqueFilePath(appId);
        //3.保存文件（具体实现交给字类）。
        saverFiles(result, uniqueFilePath);
        //4.返回文件目录对象。
        return new File(uniqueFilePath);
    }
    protected void writeToFile(String fileDirPath, String fileName,String content){
        if (StrUtil.isNotBlank(content)) {
            String filePath = fileDirPath + File.separator + fileName;
            FileUtil.writeString(content, filePath, StandardCharsets.UTF_8);
        }
    }


    /**
     * 保存文件
     * @param result 代码结果对象
     * @param uniqueFilePath 文件保存的唯一路径
     */
    protected abstract void saverFiles(T result, String uniqueFilePath);

    /**
     * 验证输入参数
     * @param result 代码结果对象
     */
    protected  void validateInput(T result){
        if(result == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR ,"代码结果对象不能为空");
        }
    }

    /** 构建文件的唯一目录路径 （tmp/code_output/bizType_雪花 ID + 雪花 ID）
     *
     * @return 构建的文件目录
     */
    private  String buildUniqueFilePath(Long appId){
        ThrowUtils.throwIf(appId == null, ErrorCode.PARAMS_ERROR, "appId不能为空");
        String codeType = getCodeType().getValue();
        String uniqueFilepath = StrUtil.format("{}_{}", codeType, appId) ;
        String dirPath =  FILE_SAVE_ROOT_DIR +  File.separator + uniqueFilepath;
        FileUtil.mkdir(dirPath);
        return dirPath;

    }
    /**
     * 获取代码生成类型
     * @return 代码生成类型枚举
     */
    protected abstract CodeGenTypeEnum getCodeType();

}
