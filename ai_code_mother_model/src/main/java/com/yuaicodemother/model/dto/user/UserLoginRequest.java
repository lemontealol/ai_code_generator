package com.yuaicodemother.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 六味lemontea 2026-01-13
 * @version 1.0
 * @description
 */
@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 密码
     */
    private String userPassword;
}
