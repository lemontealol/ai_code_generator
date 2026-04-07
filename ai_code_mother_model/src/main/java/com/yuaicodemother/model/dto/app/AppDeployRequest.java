package com.yuaicodemother.model.dto.app;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 六味lemontea 2026-01-18
 * @version 1.0
 * @description
 */
@Data
public class AppDeployRequest implements Serializable {

    /**
     * 应用 id
     */
    private Long appId;

    private static final long serialVersionUID = 1L;
}

