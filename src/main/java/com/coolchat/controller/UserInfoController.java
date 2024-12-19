package com.coolchat.controller;


import com.coolchat.domain.dto.TokenUserInfoDTO;
import com.coolchat.domain.dto.UserInfoDTO;
import com.coolchat.domain.vo.UserInfoVO;
import com.coolchat.result.Result;
import com.coolchat.service.IUserInfoService;
import com.coolchat.util.RedisComponent;
import com.coolchat.websocket.ChannelContextUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author yayibai
 * @since 2024-08-13
 */
@RestController
@RequestMapping("/userInfo")
@RequiredArgsConstructor
@Slf4j
@Api(tags = "用户相关接口")
public class UserInfoController {
    public final IUserInfoService userInfoService;
    private final RedisComponent redisComponent;
    private final ChannelContextUtils channelContextUtils;

    /**
     * 获取用户信息
     * @param request
     * @return
     */
    @GetMapping("/getUserInfo")
    @ApiOperation("获取用户信息")
    public Result<UserInfoVO> getUserInfo(HttpServletRequest request){
        return userInfoService.getUserInfo(request);
    }

    /**
     * 获取用户信息
     * @param request
     * @return
     */
    @PostMapping("/updateUserInfo")
    @ApiOperation("更新用户信息")
    public Result updateUserInfo(HttpServletRequest request, UserInfoDTO userInfoDTO) throws IOException {
        return userInfoService.updateUserInfo(request, userInfoDTO);
    }

    /**
     * 修改密码
     * @param request
     * @return
     */
    @PostMapping("/updatePassword")
    @ApiOperation("修改密码")
    public Result updatePassword(HttpServletRequest request, String password) {
        return userInfoService.updatePassword(request, password);
    }

    /**
     * 退出登录
     * @param request
     * @return
     */
    @PostMapping("/logout")
    @ApiOperation("退出登录")
    public Result logout(HttpServletRequest request) {
        TokenUserInfoDTO tokenUserInfoDTO = redisComponent.getTokenUserInfoDTO(request);
        //退出登录，关闭ws连接
        channelContextUtils.closeContact(tokenUserInfoDTO.getUserId());
        return Result.success();
    }
}
