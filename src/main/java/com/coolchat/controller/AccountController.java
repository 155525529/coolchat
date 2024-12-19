package com.coolchat.controller;

import com.coolchat.annotation.GlobalInterceptor;
import com.coolchat.domain.dto.LoginDTO;
import com.coolchat.domain.dto.MessageSendDTO;
import com.coolchat.domain.dto.RegisterDTO;
import com.coolchat.domain.dto.SysSettingDTO;
import com.coolchat.domain.vo.UserInfoVO;
import com.coolchat.result.Result;
import com.coolchat.service.IUserInfoService;
import com.coolchat.util.RedisComponent;
import com.coolchat.websocket.MessageHandler;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
@Slf4j
@Api(tags = "账户相关接口")
public class AccountController {

    private final IUserInfoService userInfoService;
    private final MessageHandler messageHandler;
    private final RedisComponent redisComponent;

    /**
     * 验证码功能
     * @return
     */
    @ApiOperation("生成验证码")
    @GetMapping("/checkCode")
    public Result<HashMap<String, String>> checkCode() {

        return userInfoService.checkCode();
    }

    /**
     * 注册功能
     * @param registerDTO
     * @return
     */
    @ApiOperation("注册")
    @PostMapping("/register")
    public Result register(RegisterDTO registerDTO){

        return userInfoService.register(registerDTO);
    }

    /**
     * 登录
     * @param loginDTO
     * @return
     */
    @ApiOperation("登录")
    @PostMapping("/login")
    public Result<UserInfoVO> login(LoginDTO loginDTO){

        return userInfoService.login(loginDTO);
    }

    @ApiOperation("获取系统设置信息")
    @GetMapping("/getSysSetting")
    @GlobalInterceptor
    public Result<SysSettingDTO> getSysSetting(){
        SysSettingDTO sysSettingDTO = redisComponent.getSysSetting();

        return Result.success(sysSettingDTO);
    }

    @PostMapping("/test")
    public Result test(){
        MessageSendDTO<Object> sendDTO = MessageSendDTO.builder()
                .messageContent("hhhhhhhhhhhhhhhhhhhh" + System.currentTimeMillis())
                .build();
        messageHandler.sendMessage(sendDTO);
        return Result.success();
    }

}
