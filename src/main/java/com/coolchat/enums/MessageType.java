package com.coolchat.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MessageType {
    INIT(0, "", "连接ws获取信息"),
    ADD_FRIEND(1, "", "添加好友打招呼消息"),
    CHAT(2, "", "普通人打招呼消息"),
    GROUP_CREATE(3, "群聊已创建好， 可以和好友一起畅聊了", "群创建成功"),
    CONTACT_APPLY(4, "", "好友申请"),
    MEDIA_CHAT(5, "", "媒体文件"),
    FILE_UPDATE(6, "", "文件上传完毕"),
    FORCE_OFF_LINE(7, "", "强制下线"),
    DISSOLUTION_GROUP(8, "群聊已解散", "解散群聊"),
    ADD_GROUP(9, "%s加入了群组", "加入群聊"),
    GROUP_NAME_UPDATE(10, "", "更新群昵称"),
    LEAVE_GROUP(11, "%s退出了群聊", "退出群聊"),
    REMOVE_GROUP(12, "%s被管理员移出群聊", "被管理员移出群聊"),
    ADD_FRIEND_SELF(13, "", "添加好友打招呼消息");

    @EnumValue
    private final Integer type;
    private final String initMessage;
    private final String desc;

    public static MessageType getByType(Integer type) {
        try {
            if (type == null) {
                return null;
            }
            for (MessageType statusEnum : MessageType.values()) {
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
