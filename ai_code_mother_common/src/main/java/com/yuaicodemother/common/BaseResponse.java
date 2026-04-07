package com.yuaicodemother.common;

import com.yuaicodemother.exception.ErrorCode;
import lombok.Data;

import java.io.Serializable;

/**
 * @author 六味lemontea 2026-01-10
 * @version 1.0
 * @description
 */
@Data
public class BaseResponse<T> implements Serializable {

    private int code;

    private T data;

    private String message;

    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public BaseResponse(int code, T data) {
        this(code, data, "");
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }
}

