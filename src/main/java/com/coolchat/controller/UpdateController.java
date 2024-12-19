package com.coolchat.controller;

import com.coolchat.annotation.GlobalInterceptor;
import com.coolchat.result.Result;
import com.coolchat.service.IAppUpdateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Api(tags = "更新相关接口")
@RequestMapping("/update")
public class UpdateController {

    private final IAppUpdateService appUpdateService;

    @PostMapping("/checkVersion")
    @ApiOperation("检查更新")
    @GlobalInterceptor
    public Result checkVersion(String version, String uid){

        return appUpdateService.checkVersion(version, uid);
    }
}
