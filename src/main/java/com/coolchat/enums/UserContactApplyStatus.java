package com.coolchat.enums;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserContactApplyStatus {
    INIT(0, "待处理"),
    PASS(1, "已同意"),
    REJECT(2, "已拒绝"),
    BLACKLIST(3, "已拉黑");

    @EnumValue
    private final Integer status;
    private final String decs;

    public static UserContactApplyStatus getByStatus(Integer status) {
        try {
            if (status == null) {
                return null;
            }
            for (UserContactApplyStatus statusEnum : UserContactApplyStatus.values()) {
                if (statusEnum.getStatus().equals(status)) {
                    return statusEnum;
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}