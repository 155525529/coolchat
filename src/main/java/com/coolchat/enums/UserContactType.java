package com.coolchat.enums;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserContactType {
    USER(0, "U", "好友"),
    Group(1, "G", "群");

    @EnumValue
    private final int type;
    private final String prefix;
    private final String desc;

    public static UserContactType getByPrefix(String prefix){

        try {
            if (StrUtil.isEmpty(prefix)){
                return null;
            }
            return UserContactType.valueOf(prefix.toUpperCase());
        }catch (Exception e){
            return null;
        }
    }


    /**
     * 通过前缀匹配类型
     * @param id
     * @return
     */
    public static UserContactType getById(String id){
        try {
            if (StrUtil.isEmpty(id) || id.trim().length() == 0){
                return null;
            }
            String prefix = id.substring(0, 1);
            for (UserContactType typeEnum : UserContactType.values()){
                if (typeEnum.getPrefix().equals(prefix)){
                    return typeEnum;
                }
            }
            return null;
        }catch (Exception e){
            return null;
        }
    }

    public static UserContactType getByNum(Integer num){
        try {
            if (num == null){
                return null;
            }
            for (UserContactType typeEnum : UserContactType.values()){
                if (typeEnum.getType() == num){
                    return typeEnum;
                }
            }
            return null;
        }catch (Exception e){
            return null;
        }
    }
}
