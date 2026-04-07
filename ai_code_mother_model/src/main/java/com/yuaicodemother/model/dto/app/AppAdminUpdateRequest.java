package com.yuaicodemother.model.dto.app;

import lombok.Data;

import java.io.Serializable;
/**
 *  管理员应用更新请求
 */
@Data
public class AppAdminUpdateRequest implements Serializable {

    private Long id;

    private String appName;

    private String cover;

    private Integer priority;

    private static final long serialVersionUID = 1L;
}

