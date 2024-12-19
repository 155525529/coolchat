package com.coolchat.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum JoinType {
    NO_CHECK(0, "直接加好友"),
    CHECK(1, "同意后加好友");

    @EnumValue
    private final Integer type;
    private final String desc;

    public static JoinType getByType(Integer type) {
        try {
            if (type == null) {
                return null;
            }
            for (JoinType statusEnum : JoinType.values()) {
                if (statusEnum.getType().equals(type)) {
                    return statusEnum;
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
