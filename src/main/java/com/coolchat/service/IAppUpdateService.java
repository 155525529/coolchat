package com.coolchat.service;

import com.coolchat.domain.dto.AppUpdateDTO;
import com.coolchat.domain.po.AppUpdate;
import com.baomidou.mybatisplus.extension.service.IService;
import com.coolchat.result.Result;

import java.io.IOException;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author yayibai
 * @since 2024-11-30
 */
public interface IAppUpdateService extends IService<AppUpdate> {

    /**
     * 保存更新
     * @param appUpdateDTO
     * @return
     */
    Result saveUpdate(AppUpdateDTO appUpdateDTO) throws IOException;

    /**
     * 发布更新
     * @param id
     * @param publishStatus
     * @param grayscaleUid
     * @return
     */
    Result postUpdate(Integer id, Integer publishStatus, String grayscaleUid);

    /**
     * 检查更新
     * @param version
     * @param uid
     * @return
     */
    Result checkVersion(String version, String uid);
}
