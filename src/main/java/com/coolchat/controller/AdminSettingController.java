package com.coolchat.controller;

import com.coolchat.annotation.GlobalInterceptor;
import com.coolchat.config.AppConfig;
import com.coolchat.constants.FileConstants;
import com.coolchat.constants.SysSettingConstants;
import com.coolchat.domain.dto.SysSettingDTO;
import com.coolchat.exception.BizIllegalException;
import com.coolchat.result.Result;
import com.coolchat.util.RedisComponent;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
@Api(tags = "管理员与系统设置相关接口")
public class AdminSettingController {
    private final RedisComponent redisComponent;
    private final AppConfig appConfig;

    /**
     * 获取系统设置
     * @return
     */
    @GetMapping("/getSysSetting")
    @ApiOperation("获取系统设置")
    @GlobalInterceptor(checkAdmin = true)
    public Result<SysSettingDTO> getSysSetting(){
        SysSettingDTO sysSettingDTO = redisComponent.getSysSetting();
        return Result.success(sysSettingDTO);
    }

    /**
     * 保存系统设置
     * @param sysSettingDTO
     * @param robotFile
     * @param robotCover
     * @return
     */
    @PostMapping("/saveSysSetting")
    @ApiOperation("保存系统设置")
    @GlobalInterceptor(checkAdmin = true)
    public Result saveSysSetting(SysSettingDTO sysSettingDTO, MultipartFile robotFile, MultipartFile robotCover) throws IOException {
        if (robotFile != null){
            String baseFolder = appConfig.getProjectFolder() + FileConstants.FILE_FOLDER;
            File targetFileFolder = new File(baseFolder + FileConstants.AVATAR_FOLDER);
            if (!targetFileFolder.exists()){
                targetFileFolder.mkdirs();
            }
            String filePath = targetFileFolder.getPath() + "/" + SysSettingConstants.ROBOT_UID + FileConstants.IMAGE_SUFFIX;
            robotFile.transferTo(new File(filePath));
            robotCover.transferTo(new File(filePath + FileConstants.COVER_IMAGE_SUFFIX));
        }
        redisComponent.saveSysSetting(sysSettingDTO);
        return Result.success(sysSettingDTO);
    }
}
