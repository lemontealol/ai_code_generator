package com.yuaicodemother.common;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 六味lemontea 2026-01-10
 * @version 1.0
 * @description
 */
@Data
public class DeleteRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}

