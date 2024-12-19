package com.coolchat.domain.dto;

import com.coolchat.enums.UserContactType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddContactDTO {
    //申请者id
    private String applyUserId;
    //联系人或群组id
    private String contactId;
    //联系人类型 0：好友 1：群
    private UserContactType contactType;
    //申请信息
    private String applyInfo;
}
