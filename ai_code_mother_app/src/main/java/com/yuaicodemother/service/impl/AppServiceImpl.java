package com.yuaicodemother.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.yuaicodemother.monitor.MonitorContext;
import com.yuaicodemother.monitor.MonitorContextHolder;
import com.yuaicodemother.ai.AiCodeGenTypeRoutingService;
import com.yuaicodemother.ai.AiCodeGenTypeRoutingServiceFactory;
import com.yuaicodemother.common.BaseResponse;
import com.yuaicodemother.common.ResultUtils;
import com.yuaicodemother.constant.AppConstant;
import com.yuaicodemother.core.AiCodeGeneratorFacade;
import com.yuaicodemother.core.builder.VueProjectBuilder;
import com.yuaicodemother.core.handler.StreamHandlerExecutor;
import com.yuaicodemother.exception.BusinessException;
import com.yuaicodemother.exception.ErrorCode;
import com.yuaicodemother.exception.ThrowUtils;
import com.yuaicodemother.mapper.AppMapper;
import com.yuaicodemother.innerservice.InnerScreenshotService;
import com.yuaicodemother.innerservice.InnerUserService;
import com.yuaicodemother.model.dto.app.AppAddRequest;
import com.yuaicodemother.model.dto.app.AppQueryRequest;
import com.yuaicodemother.model.entity.App;
import com.yuaicodemother.model.entity.User;
import com.yuaicodemother.model.enums.ChatHistoryMessageTypeEnum;
import com.yuaicodemother.model.enums.CodeGenTypeEnum;
import com.yuaicodemother.model.vo.AppVO;
import com.yuaicodemother.model.vo.UserVO;
import com.yuaicodemother.service.AppService;
import com.yuaicodemother.service.ChatHistoryService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *  服务层实现。
 *
 * @author 六味lemontea
 * @since 2026-01-16
 */
@Service
@Slf4j
public class AppServiceImpl extends ServiceImpl<AppMapper, App>  implements AppService{

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;
    @DubboReference
    private InnerUserService innerUserService;
    @Resource
    private ChatHistoryService chatHistoryService;
    @Resource
    private StreamHandlerExecutor streamHandlerExecutor;
    @Resource
    private VueProjectBuilder vueProjectBuilder;
   @DubboReference
    private InnerScreenshotService screenshotService;
    @Resource
    private AiCodeGenTypeRoutingServiceFactory aiCodeGenTypeRoutingServiceFactory;
    @Override
    public AppVO getAppVO(App app) {
        if (app == null) {
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);
        //查询关联用户信息。
        User user = innerUserService.getById(app.getUserId());
        UserVO userVO = innerUserService.getUserVO(user);
        appVO.setUserVO(userVO);
        return appVO;
    }

    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        //先在User表查询到所有的用户信息。
        Set<Long> userIdSet = appList.stream().map(App::getUserId).collect(Collectors.toSet());
        Map<Long, UserVO> collect = innerUserService.listByIds(userIdSet).stream()
                .collect(Collectors.toMap(User::getId,  innerUserService::getUserVO));
        return appList.stream().map(app ->  {
            AppVO appVO = this.getAppVO( app);
            appVO.setUserVO(collect.get(app.getUserId()));
            return appVO;
        }).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper getAppQueryWrapper(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        String appName = appQueryRequest.getAppName();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();
        Long id = appQueryRequest.getId();
        String codeGenType = appQueryRequest.getCodeGenType();
        String cover = appQueryRequest.getCover();
        String initPrompt = appQueryRequest.getInitPrompt();
        Integer priority = appQueryRequest.getPriority();
        String deployKey = appQueryRequest.getDeployKey();
        Long userId = appQueryRequest.getUserId();

        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("id", id)
                .eq("userId", userId)
                .eq("priority",priority )
                .eq("deployKey", deployKey)
                .eq("codeGenType", codeGenType)
                .like("appName", appName)
                .like("cover", cover)
                .like("initPrompt", initPrompt)
                .orderBy("updateTime", false);
        if (sortField != null && !sortField.isBlank()) {
            queryWrapper.orderBy(sortField, "ascend".equals(sortOrder));
        } else {
            queryWrapper.orderBy("createTime", false);
        }
        return queryWrapper;
    }
    @Override
    public Flux<String> chatToGenCode(Long appId, String message, User loginUser){
        //1.校验参数
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用id错误");
        ThrowUtils.throwIf(message == null, ErrorCode.PARAMS_ERROR, "提示词不能为空");
        //2.获取app信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        //3.权限校验（只有本人可以与自己的应用对话。
        ThrowUtils.throwIf(!app.getUserId().equals(loginUser.getId()), ErrorCode.NO_AUTH_ERROR, "无权限操作");
        //4.获取应用饿代码生成类型。
        String codeGenType = app.getCodeGenType();
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
        ThrowUtils.throwIf(codeGenTypeEnum == null, ErrorCode.PARAMS_ERROR, "代码生成类型错误");
        //5. 调用AI前，插入用户聊天记录。
        chatHistoryService.addChatMessage(appId, message, ChatHistoryMessageTypeEnum.USER.getValue(), loginUser.getId());
        //6. 设置监控上下文的appid和应用id
        MonitorContext monitorContext = MonitorContext.builder()
                .appId(appId.toString())
                .userId(loginUser.getId().toString())
                .build();
        MonitorContextHolder.setContext(monitorContext);
        //7.调用AI生成代码，返回流式数据。
        Flux<String>  codeFlux = aiCodeGeneratorFacade.generateAndSaveCodeStream(message, codeGenTypeEnum, appId);
        //8.保存AI返回记录。
//        //字符串拼接器，当流式返回所有的代码之后，在保存代码。
//        StringBuilder stringBuilder = new StringBuilder();
        //实时收集代码片段。
        return  streamHandlerExecutor.doExecute(codeFlux, chatHistoryService, appId, loginUser, codeGenTypeEnum)
                .doFinally(signalType -> {
                    //流式输出完成，清除监控上下文（无论成功/失败/取消）。
                    MonitorContextHolder.clearContext();
                });
    }

    @Override
    public String deployApp(Long appId,User loginUser){
        //1.校验参数
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用id错误");
        //2.获取app信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        //3.权限校验（只有本人可以与自己的应用对话。
        ThrowUtils.throwIf(!app.getUserId().equals(loginUser.getId()), ErrorCode.NO_AUTH_ERROR, "无权限操作");
        //4.检查是否已经有deployKey。如果没有生成6位的deployKey（字母+数字）。
        String deployKey = app.getDeployKey();
        //5.获取代码生成类型，获取原始代码生成路径。
        if (StrUtil.isBlank(deployKey)){
            deployKey = RandomUtil.randomString(6);
        }
        String codeGenType = app.getCodeGenType();
        String sourceName = codeGenType + "_" + appId;
        String sourcePath = Paths.get(AppConstant.CODE_OUTPUT_ROOT_DIR, sourceName).toString();
        //6.检查路径是否存在。
        File sourceDir = new File(sourcePath);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "原始应用不存在，请先生成应用");
        }
        //7.Vue 项目特殊处理：执行构建。
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
        if (codeGenTypeEnum == CodeGenTypeEnum.VUE_PROJECT) {
            boolean buildSuccess = vueProjectBuilder.buildProject(sourcePath);
            ThrowUtils.throwIf(!buildSuccess, ErrorCode.SYSTEM_ERROR, "Vue 构建失败，请重试。");
            //检查dist 目录是否存在。
            File distDir = new File(sourcePath, "dist");
            ThrowUtils.throwIf(!distDir.exists() || !distDir.isDirectory(), ErrorCode.SYSTEM_ERROR, "Vue 构建失败，请重试。");
            //构建完成后，需要将构建后的文件复制到部署目录。
            sourceDir = distDir;
        }
        //7.复制文件到部署目录。
        String deployPath = Paths.get(AppConstant.CODE_DEPLOY_ROOT_DIR, deployKey).toString();
        try {
            FileUtil.copyContent(sourceDir, new File(deployPath),true);
        } catch (IORuntimeException e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "部署失败 " + e.getMessage());
        }
        //8.更新数据库。
        App updateApp = new App();
        updateApp.setId(appId);
        updateApp.setDeployKey(deployKey);
        updateApp.setDeployedTime(LocalDateTime.now());
        boolean updateResult = this.updateById(updateApp);
        ThrowUtils.throwIf(!updateResult, ErrorCode.OPERATION_ERROR,"更新app部署数据失败。");
        //9.生成可访问的URL。
       String deployUrl = String.format("%s/%s",AppConstant.CODE_DEPLOY_HOST,deployKey);
       //10.异步截图、并且更新应用封面。
        generateAppScreenshotAsync(appId, deployUrl);
        return deployUrl;
    }

    public void generateAppScreenshotAsync(Long appId, String deployUrl){
        //创建一个虚拟线程，并执行截图服务，截图成功后，更新数据库的封面。
        Thread.startVirtualThread(() -> {
            //截图并上传
            String screenshotUrl = screenshotService.generateAndUploadScreenshot(deployUrl);
            //更新数据库的封面。
            App updateApp = new App();
            updateApp.setId(appId);
            updateApp.setCover(screenshotUrl);
            boolean updated = this.updateById(updateApp);
            ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR, "更新app封面失败。");
        });
    }

    /**
     * 删除应用，关联删除应用下的所有聊天记录。
     * @param id
     * @return
     */
    @Override
    public boolean removeById(Serializable id) {
        //1.校验参数
        ThrowUtils.throwIf(id == null || Long.parseLong(id.toString()) <= 0L, ErrorCode.PARAMS_ERROR, "应用id错误");
        //2.获取app信息
        App app = this.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        try {
            chatHistoryService.removeById(app);
        }catch (Exception e){
            log.error("删除应用关联的聊天记录失败： {}",e.getMessage());
        }
        try {
            this.removeById(app);
        }catch (Exception e){
            log.error("删除应用失败： {}",e.getMessage());
        }
        return true;
    }
    @Override
    public BaseResponse<Long> createApp(AppAddRequest appAddRequest, HttpServletRequest request){
        //参数校验
        ThrowUtils.throwIf(appAddRequest == null, ErrorCode.PARAMS_ERROR);
        String initPrompt = appAddRequest.getInitPrompt();
        ThrowUtils.throwIf(StrUtil.isBlank(initPrompt), ErrorCode.PARAMS_ERROR, "initPrompt 不能为空");
        //获取当前登录用户。
        User loginUser = InnerUserService.getLoginUser(request);
        //构造入库对象
        App app = new App();
        BeanUtil.copyProperties(appAddRequest, app);
        //ai根据提示词获取生成代码类型。
        AiCodeGenTypeRoutingService aiCodeGenTypeRoutingService = aiCodeGenTypeRoutingServiceFactory.createAiCodeGenTypeRoutingService();
        CodeGenTypeEnum codeGenTypeEnum = aiCodeGenTypeRoutingService.routeCodeGenType(initPrompt);
        app.setCodeGenType(codeGenTypeEnum.getValue());
        app.setPriority(AppConstant.DEFAULT_APP_PRIORITY );
        app.setUserId(loginUser.getId());
        //应用暂时为 initPrompt 前 12 位。
        app.setInitPrompt(initPrompt/*.substring(0, Math.min(initPrompt.length(), 12))*/);
        if (app.getPriority() == null) {
            app.setPriority(0);
        }
        //插入数据库。
        boolean result = this.save(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(app.getId());
    }
  /*  @Override
    public QueryWrapper getFeaturedAppQueryWrapper(AppFeaturedQueryRequest appFeaturedQueryRequest) {
        if (appFeaturedQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        String appName = appFeaturedQueryRequest.getAppName();
        QueryWrapper queryWrapper = QueryWrapper.create()
                .gt("priority", 0)
                .like("appName", appName)
                .orderBy("priority", false)
                .orderBy("updateTime", false);
        return queryWrapper;
    }*/
}
