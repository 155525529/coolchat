package com.coolchat.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PublishStatus {
    INIT(0, "未发布"),
    GRAYSCALE(1, "灰度发布"),
    WHOLE_NETWORK(2, "全网发布");



    @EnumValue
    private final Integer status;
    private final String desc;

    public static PublishStatus getByStatus(Integer status) {
        try {
            if (status == null) {
                return null;
            }
            for (PublishStatus statusEnum : PublishStatus.values()) {
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
