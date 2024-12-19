package com.coolchat.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OperationType {
    REMOVE(0, "删除"),
    ADD(1, "添加");

    private final Integer type;
    private final String desc;

    public static OperationType getByType(Integer type) {
        try {
            if (type == null) {
                return null;
            }
            for (OperationType statusEnum : OperationType.values()) {
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
