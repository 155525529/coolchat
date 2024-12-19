package com.coolchat.domain.vo;

import com.coolchat.enums.UserContactApplyStatus;
import com.coolchat.enums.UserContactType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApplySearchVO {
    /**
     * 申请人id
     */
    private String applyUserId;

    /**
     * 接收人id
     */
    private String receiveUserId;

    /**
     * 联系人类型 0:好友 1:群组
     */
    private UserContactType contactType;

    /**
     * 联系人群组id
     */
    private String contactId;

    /**
     * 最后申请时间
     */
    private LocalDateTime lastApplyTime;

    /**
     * 状态 0:待处理 1:已同意 2:已拒绝 3:已拉黑
     */
    private UserContactApplyStatus status;

    /**
     * 申请信息
     */
    private String applyInfo;

    /**
     * 申请人名或群名
     */
    private String contactName;
}
