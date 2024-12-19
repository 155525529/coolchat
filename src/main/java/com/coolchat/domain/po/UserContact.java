package com.coolchat.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import com.coolchat.enums.UserContactStatus;
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
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user_contact")
@Builder
public class UserContact implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户id
     */
    @TableId(value = "user_id", type = IdType.INPUT)
    private String userId;

    /**
     * 联系人id或群组id
     */
    private String contactId;

    /**
     * 联系人类型 0:好友 1:群组
     */
    private UserContactType contactType;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 状态 0:非好友 1:好友 2:已删除好友 3:被好友删除 4:已拉黑好友 5:被好友拉黑 6:首次被好友拉黑
     */
    private UserContactStatus status;

    /**
     * 最后更新时间
     */
    private LocalDateTime lastUpdateTime;


}
