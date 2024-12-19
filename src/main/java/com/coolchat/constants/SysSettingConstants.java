package com.coolchat.constants;

import com.coolchat.enums.UserContactType;

public class SysSettingConstants {

    public static final String ROBOT_UID = UserContactType.USER.getPrefix() + "robot";
    public static final Integer MAX_GROUP_COUNT = 10;
    public static final Integer MAX_GROUP_MEMBER_COUNT = 500;
    public static final Integer MAX_IMAGE_SIZE = 20;
    public static final Integer MAX_VIDEO_SIZE = 500;
    public static final Integer MAX_FILE_SIZE = 500;
    public static final String ROBOT_NICK_NAME = "coolchat";
    public static final String ROBOT_WELCOME = "欢迎使用coolchat";
}
