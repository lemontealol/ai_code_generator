package com.yuaicodemother.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.yuaicodemother.common.BaseResponse;
import com.yuaicodemother.model.dto.app.AppAddRequest;
import com.yuaicodemother.model.dto.app.AppQueryRequest;
import com.yuaicodemother.model.entity.App;
import com.yuaicodemother.model.entity.User;
import com.yuaicodemother.model.vo.AppVO;
import jakarta.servlet.http.HttpServletRequest;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 *  服务层。
 *
 * @author 六味lemontea
 * @since 2026-01-16
 */
public interface AppService extends IService<App> {

    AppVO getAppVO(App app);

    List<AppVO> getAppVOList(List<App> appList);


    QueryWrapper getAppQueryWrapper(AppQueryRequest appQueryRequest);

    /**
     * 对话生成代码
     *
     * @param appId 应用ai
     * @param message 用户信息
     * @param loginUser 登录用户
     * @return 返回生成的流式 数据。
     */
    Flux<String> chatToGenCode(Long appId, String message, User loginUser);
    /**
     * 应用部署
     *
     * @param appId 应用id
     * @param loginUser 登录用户
     * @return 可访问的部署地址。
     */
    String deployApp(Long appId,User loginUser);
    /**
     * 异步生成应用截图
     *
     * @param appId 应用id
     * @param deployUrl 应用部署地址
     */
    void generateAppScreenshotAsync(Long appId, String deployUrl);

    BaseResponse<Long> createApp(AppAddRequest appAddRequest, HttpServletRequest request);
}
