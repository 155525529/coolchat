package com.coolchat.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coolchat.annotation.GlobalInterceptor;
import com.coolchat.domain.dto.PageDTO;
import com.coolchat.domain.po.GroupInfo;
import com.coolchat.domain.query.PageQuery;
import com.coolchat.result.Result;
import com.coolchat.service.IGroupInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
@Api(tags = "管理员与群组相关接口")
public class AdminGroupInfoController {

    private final IGroupInfoService groupInfoService;

    /**
     * 获取群组列表
     * @param query
     * @return
     */
    @GetMapping("/loadGroup")
    @ApiOperation("获取群组列表")
    @GlobalInterceptor(checkAdmin = true)
    public Result<PageDTO<GroupInfo>> loadGroup(PageQuery query){
        Page<GroupInfo> page = query.toMpPage("create_time", false);
        groupInfoService.page(page);
        PageDTO<GroupInfo> of = PageDTO.of(page, GroupInfo.class);

        return Result.success(of);
    }

    /**
     * 解散群组
     * @param groupId
     * @return
     */
    @PostMapping("/dissolutionGroup")
    @ApiOperation("解散群组")
    @GlobalInterceptor(checkAdmin = true)
    public Result dissolutionGroup(String groupId){

        return groupInfoService.dissolutionGroup(groupId, null, true);
    }
}
