package com.yuaicodemother.ai.model.message;

/**
 * @author 六味lemontea 2026-01-24
 * @version 1.0
 * @description
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 流式消息响应基类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StreamMessage {
    private String type;
}

