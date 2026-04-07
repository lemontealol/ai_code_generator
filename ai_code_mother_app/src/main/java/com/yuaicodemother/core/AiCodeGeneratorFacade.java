package com.yuaicodemother.core;

import cn.hutool.json.JSONUtil;
import com.yuaicodemother.ai.AiCodeGeneratorService;
import com.yuaicodemother.ai.AiCodeGeneratorServiceFactory;
import com.yuaicodemother.ai.model.message.AiResponseMessage;
import com.yuaicodemother.ai.model.message.ToolExecutedMessage;
import com.yuaicodemother.ai.model.message.ToolRequestMessage;
import com.yuaicodemother.constant.AppConstant;
import com.yuaicodemother.core.builder.VueProjectBuilder;
import com.yuaicodemother.core.parser.CodeParserExecutor;
import com.yuaicodemother.core.saver.CodeFileSaverExecutor;
import com.yuaicodemother.exception.BusinessException;
import com.yuaicodemother.exception.ErrorCode;
import com.yuaicodemother.model.enums.CodeGenTypeEnum;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolExecution;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * AI 代码生成门面类，组合代码生成和保存功能。
 * @author 六味lemontea 2026-01-14
 * @version 1.0
 * @description
 */
@Service
@Slf4j
public class AiCodeGeneratorFacade {
    @Resource
    private VueProjectBuilder vueProjectBuilder;

    @Resource
    private AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;
    /**
     * 统一入口：根据类型生成代码并保存
     * @param userMessage 用户信息
     * @param codeGenTypeEnum 代码生成类型
     * @return 保存后的文件
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum,Long appId){
        if (codeGenTypeEnum == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"生成代码类型不能为空");
        }
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appId, codeGenTypeEnum);
        Flux<String> file;
        switch (codeGenTypeEnum){
            case HTML -> file = generateHtmlCodeStream(aiCodeGeneratorService,userMessage,appId);
            case MULTI_FILE -> file = generateMultiFileCodeStream(aiCodeGeneratorService,userMessage,appId);
            case VUE_PROJECT -> file = generateVueCodeStream(aiCodeGeneratorService,userMessage,appId);
            default -> {
                String message = "不支持的代码生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.PARAMS_ERROR, message);
            }
        }
        return file;
    }

    /**
     *处理不同类型的生成代码。
     * @param codeStream 代码流式对象
     * @param codeGenType 生成的代码类型
     * @return 代码流式对象
     */
    private Flux<String> processCodeStream(Flux<String> codeStream,CodeGenTypeEnum codeGenType,Long appId) {
        //字符串拼接器，当流式返回所有的代码之后，在保存代码。
        StringBuilder stringBuilder = new StringBuilder();
        //实时收集代码片段。
        return codeStream.doOnNext(stringBuilder::append).doOnComplete(() -> {
            try {
                //当流式返回所有代码片段之后，调用保存代码的方法。
                String completeCode = stringBuilder.toString();
                Object htmlCodeResult = CodeParserExecutor.executeParser(completeCode, codeGenType);
                //使用代码保存执行器保存代码到文件。
                File file = CodeFileSaverExecutor.saveCodeResult(htmlCodeResult,codeGenType,appId);
                log.info("保存文件成功：{}", file.getAbsolutePath());
            } catch (Exception e) {
                log.error("保存文件失败：{}", e.getMessage());
            }
        });
    }
     /**
     * 生成多文件代码并保存
     * @param userMessage 用户信息
     * @return 保存的文件
     */
    private Flux<String> generateHtmlCodeStream(AiCodeGeneratorService aiCodeGeneratorService,String userMessage,Long appId) {
        Flux<String> stringFlux = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
        return processCodeStream(stringFlux,CodeGenTypeEnum.HTML,appId);
    }
    /**
     * 生成多文件代码并保存
     * @param userMessage 用户信息
     * @return 保存的文件
     */
    private Flux<String> generateMultiFileCodeStream(AiCodeGeneratorService aiCodeGeneratorService,String userMessage,Long appId) {
        Flux<String> stringFlux = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
        return processCodeStream(stringFlux,CodeGenTypeEnum.MULTI_FILE,appId);
    }
    /**
     * 生成VUE工程项目代码并保存
     * @param userMessage 用户信息
     * @return 保存的文件
     */
    private Flux<String> generateVueCodeStream(AiCodeGeneratorService aiCodeGeneratorService,String userMessage,Long appId) {
        TokenStream tokenStream = aiCodeGeneratorService.generateVueProjectCodeStream(appId,userMessage);
        return processTokenStream(tokenStream,appId);
    }
    /**
     * 将 TokenStream 转换为 Flux<String>，并传递工具调用信息
     *
     * @param tokenStream TokenStream 对象
     * @return Flux<String> 流式响应
     */
    private Flux<String> processTokenStream(TokenStream tokenStream,Long appId) {
        return Flux.create(sink -> {
            tokenStream.onPartialResponse((String partialResponse) -> {
                        AiResponseMessage aiResponseMessage = new AiResponseMessage(partialResponse);
                        sink.next(JSONUtil.toJsonStr(aiResponseMessage));
                    })
                    .onPartialToolExecutionRequest((index, toolExecutionRequest) -> {
                        ToolRequestMessage toolRequestMessage = new ToolRequestMessage(toolExecutionRequest);
                        sink.next(JSONUtil.toJsonStr(toolRequestMessage));
                    })
                    .onToolExecuted((ToolExecution toolExecution) -> {
                        ToolExecutedMessage toolExecutedMessage = new ToolExecutedMessage(toolExecution);
                        sink.next(JSONUtil.toJsonStr(toolExecutedMessage));
                    })
                    .onCompleteResponse((ChatResponse response) -> {
                        // 执行 Vue 项目构建（同步执行，确保预览时项目已就绪）
                        String projectPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + "vue_project_" + appId;
                        vueProjectBuilder.buildProject(projectPath);
                        sink.complete();
                    })

                    .onError((Throwable error) -> {
                        error.printStackTrace();
                        sink.error(error);
                    })
                    .start();
        });
    }


/* private File generateMultiFileCode(String userMessage) {
        MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userMessage);
        return CodeFileSaver.saveMultiFileCodeResult(result);
    }

    private File generateHtmlCode(String userMessage) {
        HtmlCodeResult htmlCodeResult = aiCodeGeneratorService.generateHtmlCode(userMessage);
        return CodeFileSaver.saveHtmlCodeResult(htmlCodeResult);
    }*/
}
