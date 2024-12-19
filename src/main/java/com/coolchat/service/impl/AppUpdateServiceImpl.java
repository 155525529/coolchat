package com.coolchat.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.coolchat.config.AppConfig;
import com.coolchat.constants.FileConstants;
import com.coolchat.domain.dto.AppUpdateDTO;
import com.coolchat.domain.po.AppUpdate;
import com.coolchat.domain.vo.AppUpdateVO;
import com.coolchat.enums.FileType;
import com.coolchat.enums.PublishStatus;
import com.coolchat.exception.BizIllegalException;
import com.coolchat.mapper.AppUpdateMapper;
import com.coolchat.result.Result;
import com.coolchat.service.IAppUpdateService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coolchat.util.VersionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author yayibai
 * @since 2024-11-30
 */
@Service
@RequiredArgsConstructor
public class AppUpdateServiceImpl extends ServiceImpl<AppUpdateMapper, AppUpdate> implements IAppUpdateService {
    private final AppUpdateMapper appUpdateMapper;
    private final AppConfig appConfig;

    /**
     * 保存更新
     * @param appUpdateDTO
     * @return
     */
    @Override
    public Result saveUpdate(AppUpdateDTO appUpdateDTO) throws IOException {
        FileType type = FileType.getByType(appUpdateDTO.getFileType());
        if (type == null){
            throw new BizIllegalException("错误输入");
        }

        LambdaQueryWrapper<AppUpdate> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(AppUpdate::getId);
        List<AppUpdate> appUpdateList = appUpdateMapper.selectList(wrapper);

        //如果状态不是未发布则不允许修改
        if (appUpdateDTO.getId() != null){
            AppUpdate appUpdate = appUpdateMapper.selectById(appUpdateDTO.getId());
            if (!PublishStatus.INIT.equals(appUpdate.getStatus())){
                throw new BizIllegalException("非法操作");
            }
        }

        //检查版本号输入是否正确
        if (!appUpdateList.isEmpty()){

            AppUpdate latest = appUpdateList.get(0);
            String latestVersion = latest.getVersion();
            String newVersion = appUpdateDTO.getVersion();

            int compared = VersionUtil.compareVersions(latestVersion, newVersion);

            //新建时版本号是否大于最大版本号
            if (appUpdateDTO.getId() == null && compared == 1){
                throw new BizIllegalException("版本号输入错误");
            }
            //更新时版本号是否小于最大版本号
            AppUpdate appUpdate1 = lambdaQuery().eq(AppUpdate::getId, appUpdateDTO.getId()).one();
            if (appUpdateDTO.getId() != null && appUpdate1 != null){
                throw new BizIllegalException("未找到版本id");
            }
            if (appUpdateDTO.getId() != null && !appUpdateDTO.getId().equals(latest.getId()) && compared == -1 || compared == 0){
                throw new BizIllegalException("版本号输入错误");
            }
            //是否有相同版本号
            AppUpdate appUpdate = lambdaQuery().eq(AppUpdate::getVersion, appUpdateDTO.getVersion()).one();
            if (appUpdate != null){
                throw new BizIllegalException("有相同版本号");
            }
        }

        //保存更新
        AppUpdate appUpdate = BeanUtil.copyProperties(appUpdateDTO, AppUpdate.class);
        appUpdate.setCreateTime(LocalDateTime.now());
        appUpdate.setStatus(PublishStatus.INIT);

        if (appUpdateDTO.getId() == null){
            //insert
            appUpdateMapper.insert(appUpdate);
        }else {
            //update
            LambdaUpdateWrapper<AppUpdate> wrapper1 = new LambdaUpdateWrapper<>();
            wrapper1.eq(AppUpdate::getId, appUpdateDTO.getId());
            appUpdateMapper.update(appUpdate, wrapper1);
        }

        if (appUpdateDTO.getFile() != null){
            File folder = new File(appConfig.getProjectFolder() + FileConstants.APP_UPDATE_FOLDER);
            if (!folder.exists()){
                folder.mkdirs();
            }
            appUpdateDTO.getFile().transferTo(new File(folder.getAbsolutePath() + "/" + appUpdateDTO.getId() + FileConstants.APP_EXE_SUFFIX));
        }

        return Result.success();
    }

    /**
     * 发布更新
     * @param id
     * @param publishStatus
     * @param grayscaleUid
     * @return
     */
    @Override
    public Result postUpdate(Integer id, Integer publishStatus, String grayscaleUid) {
        PublishStatus status = PublishStatus.getByStatus(publishStatus);
        if (status == null){
            throw new BizIllegalException("输入异常");
        }
        if (PublishStatus.GRAYSCALE.equals(status) && grayscaleUid.isEmpty()){
            throw new BizIllegalException("未找到参数");
        }

        //如果不是灰度发布则将灰度uid置空
        if (!PublishStatus.GRAYSCALE.equals(status)){
            grayscaleUid = "";
        }

        lambdaUpdate().eq(AppUpdate::getId, id)
                .set(AppUpdate::getStatus, publishStatus)
                .set(AppUpdate::getGrayscaleUid, grayscaleUid)
                .update();
        return Result.success();
    }

    @Override
    public Result checkVersion(String version, String uid) {
        if (version.isEmpty()){
            return Result.success();
        }

        AppUpdate appUpdate = appUpdateMapper.getLatestUpdate(version, uid);
        if (appUpdate == null){//无新版本
            return Result.success();
        }
        AppUpdateVO appUpdateVO = BeanUtil.copyProperties(appUpdate, AppUpdateVO.class);

        if (FileType.LOCAL.equals(appUpdate.getFileType())){
            File file = new File(appConfig.getProjectFolder() + FileConstants.APP_UPDATE_FOLDER + appUpdate.getId() + FileConstants.APP_EXE_SUFFIX);
            appUpdateVO.setSize(file.length());
        }else {
            appUpdateVO.setSize(0L);
        }
        appUpdateVO.setUpdateDesc(appUpdate.getUpdateDesc());
        String fileName = FileConstants.APP_NAME + appUpdate.getVersion() + FileConstants.APP_EXE_SUFFIX;
        appUpdateVO.setFileName(fileName);

        return Result.success(appUpdateVO);
    }
}
