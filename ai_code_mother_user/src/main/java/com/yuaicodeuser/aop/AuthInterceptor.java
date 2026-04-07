package com.yuaicodeuser.aop;

import com.yuaicodemother.annotation.AuthCheck;
import com.yuaicodemother.exception.BusinessException;
import com.yuaicodemother.exception.ErrorCode;
import com.yuaicodemother.model.entity.User;
import com.yuaicodemother.model.enums.UserRoleEnum;
import com.yuaicodeuser.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @author 六味lemontea 2026-01-12
 * @version 1.0
 * @description
 */
@Aspect
@Component
public class AuthInterceptor {
    @Resource
    private UserService userService;
    /**
     * 拦截器
     *
     * @param joinPoint 切入点
     * @param authCheck 权限校验注解
     * @return
     * @throws Throwable
     */
    @Around("@annotation(authCheck)") //对这个注解进行拦截。
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        String mustRole = authCheck.mustRole();
        //获取当前登录用户
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest httpServletRequest = ((ServletRequestAttributes) requestAttributes).getRequest();
        User loginUser = userService.getLoginUser(httpServletRequest);
        //不需要权限，直接放行。
        if (mustRole == null) {
            return joinPoint.proceed();
        }
        //下面的代码，有权限才能登陆。
        UserRoleEnum userRoleEnum = UserRoleEnum.getEnumByValue(loginUser.getUserRole());
        if (userRoleEnum == null){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        //要求有管理员权限。
        if ( userRoleEnum != UserRoleEnum.ADMIN){
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        //要求有普通用户权限。
//        if (userRoleEnum != UserRoleEnum.USER){
//                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
//        }
        return joinPoint.proceed();

    }
}
