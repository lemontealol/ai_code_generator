package com.yuaicodemother.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.yuaicodemother.model.dto.chatHistory.ChatHistoryQueryRequest;
import com.yuaicodemother.model.entity.ChatHistory;
import com.yuaicodemother.model.entity.User;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

import java.time.LocalDateTime;

/**
 *  服务层。
 *
 * @author 六味lemontea
 * @since 2026-01-16
 */
public interface ChatHistoryService extends IService<ChatHistory> {
    /**
     * 添加聊天记录
     *
     * @param appId 应用id
     * @param userId 用户id
     * @param message 消息
     * @param messageType 消息类型
     * @return 是否添加成功
     */

    boolean addChatMessage(Long appId, String message, String messageType, Long userId);
    /**
     * 根据appId删除聊天记录
     *
     * @param appId 应用id
     * @return 是否删除成功
     */
    boolean deleteChatMessage(Long appId);

    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);

    Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,
                                               LocalDateTime lastCreateTime,
                                               User loginUser);

    int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount);
}
