package com.coolchat.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.coolchat.exception.BadRequestException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserStatus {
    FROZEN(0, "禁止使用"),
    NORMAL(1, "已激活"),
    ;
    @EnumValue
    private final int value;
    private final String desc;


    public static UserStatus getByNum(Integer value) {
        if (value == 0) {
            return FROZEN;
        }
        if (value == 1) {
            return NORMAL;
        }
        throw new BadRequestException("错误请求");
    }
}