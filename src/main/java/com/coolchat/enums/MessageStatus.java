package com.coolchat.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MessageStatus {
    SENDING(0, "正在发送"),
    SENT(1, "已发送");

    @EnumValue
    private final int status;
    private final String desc;
}
