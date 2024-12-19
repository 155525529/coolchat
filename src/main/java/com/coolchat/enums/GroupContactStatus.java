package com.coolchat.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GroupContactStatus {
    NORMAL(1, "正常"),
    DISSOLVE(0, "解散");

    @EnumValue
    private final int type;
    private final String desc;
}
