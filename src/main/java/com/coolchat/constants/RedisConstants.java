package com.coolchat.constants;

import com.coolchat.enums.UserContactType;

public class RedisConstants {
    public static final String CHECK_CODE_KEY = "coolchat:checkCode:";
    public static final Long CHECK_CODE_TTL = 60L;
    public static final String WS_USER_HEART_BEAT_KEY = "coolchat:ws:user:heartbeat:";
    public static final Long WS_USER_HEART_BEAT_TTL = 6L;
    public static final String WS_TOKEN_KEY = "coolchat:ws:token:";
    public static final String WS_TOKEN_USERID_KEY = "coolchat:ws:token:userid:";
    public static final Long WS_TOKEN_TTL = 1L;
    public static final String SYS_SETTING_KEY = "coolchat:sysSetting:";
    public static final String USER_CONTACT_KEY = "coolchat:ws:user:contact";


}
