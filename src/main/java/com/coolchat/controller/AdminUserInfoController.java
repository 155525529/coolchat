package com.coolchat.controller;

import com.coolchat.annotation.GlobalInterceptor;
import com.coolchat.domain.dto.PageDTO;
import com.coolchat.domain.query.PageQuery;
import com.coolchat.domain.vo.UserInfoVO;
import com.coolchat.enums.UserStatus;
import com.coolchat.result.Result;
import com.coolchat.service.IUserInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
@Api(tags = "管理员与用户相关接口")
public class AdminUserInfoController {

    private final IUserInfoService userInfoService;

    /**
     * 获取用户列表
     * @param query
     * @return
     */
    @GetMapping("/loadUser")
    @ApiOperation("获取用户列表")
    @GlobalInterceptor(checkAdmin = true)
    public Result<PageDTO<UserInfoVO>> loadUser(PageQuery query){
        return userInfoService.loadUser(query);
    }


    /**
     * 跟新用户状态
     * @param status
     * @param userId
     * @return
     */
    @PostMapping("/updateUserStatus")
    @ApiOperation("更新用户状态")
    @GlobalInterceptor(checkAdmin = true)
    public Result updateUserStatus(Integer status, String userId){
        return userInfoService.updateUserStatus(status, userId);
    }

    /**
     * 强制下线
     * @param userId
     * @return
     */
    @PostMapping("/forceOffLine")
    @ApiOperation("强制下线")
    @GlobalInterceptor(checkAdmin = true)
    public Result forceOffLine(String userId){
        return userInfoService.forceOffLine(userId);
    }
}
