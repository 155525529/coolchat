package com.coolchat.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FileType {
    LOCAL(0, "本地文件"),
    OUTSIDE(1, "外链");

    @EnumValue
    private final Integer type;
    private final String desc;

    public static FileType getByType(Integer type) {
        try {
            if (type == null) {
                return null;
            }
            for (FileType fileTypeEnum : FileType.values()) {
                if (fileTypeEnum.getType().equals(type)) {
                    return fileTypeEnum;
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
