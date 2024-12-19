package com.coolchat.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.coolchat.enums.UserContactApplyStatus;
import com.coolchat.enums.UserContactType;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author yayibai
 * @since 2024-08-20
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user_contact_apply")
public class UserContactApply implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 自增id
     */
    @TableId(value = "apply_id", type = IdType.AUTO)
    private Integer applyId;

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


}
