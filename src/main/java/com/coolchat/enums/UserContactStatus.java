package com.coolchat.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;

@Getter
@AllArgsConstructor
public enum UserContactStatus {
    NOT_FRIEND(0, "非好友"),
    FRIEND(1, "好友"),
    DEL(2, "已删除好友"),
    BE_DEL(3, "被好友删除"),
    BLOCK(4, "已拉黑好友"),
    BE_BLOCK(5, "被好友拉黑"),
    BE_BLOCK_FIRST(6, "首次被好友拉黑");

    @EnumValue
    private final Integer status;
    private final String desc;
}
