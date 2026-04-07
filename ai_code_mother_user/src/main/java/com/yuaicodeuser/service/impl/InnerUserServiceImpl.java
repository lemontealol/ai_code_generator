package com.yuaicodeuser.service.impl;

import com.yuaicodemother.innerservice.InnerUserService;
import com.yuaicodemother.model.entity.User;
import com.yuaicodemother.model.vo.UserVO;
import com.yuaicodeuser.service.UserService;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * @author 六味lemontea 2026-03-04
 * @version 1.0
 * @description
 */
@DubboService
public class InnerUserServiceImpl implements InnerUserService {

    @Resource
    private UserService userService;

    @Override
    public List<User> listByIds(Collection<? extends Serializable> ids) {
        return userService.listByIds(ids);
    }

    @Override
    public User getById(Serializable id) {
        return userService.getById(id);
    }

    @Override
    public UserVO getUserVO(User user) {
        return userService.getUserVO(user);
    }
}

