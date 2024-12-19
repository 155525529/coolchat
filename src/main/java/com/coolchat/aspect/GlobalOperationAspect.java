package com.coolchat.aspect;

import cn.hutool.core.util.StrUtil;
import com.coolchat.annotation.GlobalInterceptor;
import com.coolchat.constants.RedisConstants;
import com.coolchat.domain.dto.TokenUserInfoDTO;
import com.coolchat.exception.BizIllegalException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
@Slf4j
public class GlobalOperationAspect {

    @Autowired
    private RedisTemplate redisTemplate;

    @Before("@annotation(com.coolchat.annotation.GlobalInterceptor)")
    public void interceptorDo(JoinPoint joinPoint){
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            GlobalInterceptor globalInterceptor = signature.getMethod().getAnnotation(GlobalInterceptor.class);
            if (globalInterceptor == null){
                return;
            }
            if (globalInterceptor.checkLogin() || globalInterceptor.checkAdmin()){
                checkLogin(globalInterceptor.checkAdmin());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void checkLogin(Boolean checkAdmin){
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        String token = request.getHeader("token");
        if (StrUtil.isEmpty(token)){
            throw new BizIllegalException("服务异常1");
        }
        TokenUserInfoDTO tokenUserInfoDTO = (TokenUserInfoDTO) redisTemplate.opsForValue().get(RedisConstants.WS_TOKEN_KEY + token);
        if (tokenUserInfoDTO == null){
            throw new BizIllegalException("服务异常2");
        }
        if (checkAdmin && !tokenUserInfoDTO.getAdmin()){
            throw new BizIllegalException("服务异常3");
        }
    }
}
