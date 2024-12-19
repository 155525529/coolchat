package com.coolchat.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coolchat.annotation.GlobalInterceptor;
import com.coolchat.domain.dto.PageDTO;
import com.coolchat.domain.dto.AppUpdateDTO;
import com.coolchat.domain.po.AppUpdate;
import com.coolchat.domain.query.PageQuery;
import com.coolchat.enums.PublishStatus;
import com.coolchat.exception.BizIllegalException;
import com.coolchat.result.Result;
import com.coolchat.service.IAppUpdateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author yayibai
 * @since 2024-11-30
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Api(tags = "更新相关接口")
public class AdminAppUpdateController {
    private final IAppUpdateService appUpdateService;

    @GetMapping("/loadUpdateList")
    @ApiOperation("获取更新列表")
    @GlobalInterceptor(checkAdmin = true)
    public Result<PageDTO<AppUpdate>> loadUpdateList(PageQuery query){
        Page<AppUpdate> page = query.toMpPage("id", false);
        appUpdateService.page(page);
        PageDTO<AppUpdate> of = PageDTO.of(page, AppUpdate.class);

        return Result.success(of);
    }

    @PostMapping("/saveUpdate")
    @ApiOperation("保存更新")
    @GlobalInterceptor(checkAdmin = true)
    public Result saveUpdate(AppUpdateDTO appUpdateDTO) throws IOException {
        return appUpdateService.saveUpdate(appUpdateDTO);
    }

    @DeleteMapping("/delUpdate")
    @ApiOperation("删除更新")
    @GlobalInterceptor(checkAdmin = true)
    public Result delUpdate(Integer id){
        //如果状态不是未发布则不让删除
        AppUpdate db = appUpdateService.getById(id);
        if (PublishStatus.INIT.equals(db.getStatus())){
            throw new BizIllegalException("非法操作");
        }
        appUpdateService.removeById(id);
        return Result.success();
    }

    @PostMapping("/postUpdate")
    @ApiOperation("发布更新")
    @GlobalInterceptor(checkAdmin = true)
    public Result postUpdate(Integer id, Integer publishStatus, String grayscaleUid){

        return appUpdateService.postUpdate(id, publishStatus, grayscaleUid);
    }
}
