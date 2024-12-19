package com.coolchat.domain.dto;

import com.coolchat.constants.SysSettingConstants;
import lombok.Data;

import java.io.Serializable;

@Data
public class SysSettingDTO implements Serializable {
    private Integer maxGroupCount = SysSettingConstants.MAX_GROUP_COUNT;
    private Integer maxGroupMemberCount = SysSettingConstants.MAX_GROUP_MEMBER_COUNT;
    private Integer maxImageSize = SysSettingConstants.MAX_IMAGE_SIZE;
    private Integer maxVideoSize = SysSettingConstants.MAX_VIDEO_SIZE;
    private Integer maxFileSize = SysSettingConstants.MAX_FILE_SIZE;
    private String robotUid = SysSettingConstants.ROBOT_UID;
    private String robotNickName = SysSettingConstants.ROBOT_NICK_NAME;
    private String robotWelcome = SysSettingConstants.ROBOT_WELCOME;
}
