package com.coolchat.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.coolchat.enums.FileType;
import com.coolchat.enums.MessageStatus;
import com.coolchat.enums.MessageType;
import com.coolchat.enums.UserContactType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
 * @since 2024-12-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("chat_message")
@ApiModel(value="ChatMessage对象", description="")
@Builder
public class ChatMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "消息自增id")
    @TableId(value = "message_id", type = IdType.AUTO)
    private Long messageId;

    @ApiModelProperty(value = "会话id")
    private String sessionId;

    @ApiModelProperty(value = "消息类型")
    private MessageType messageType;

    @ApiModelProperty(value = "消息内容")
    private String messageContent;

    @ApiModelProperty(value = "发送人ID")
    private String sendUserId;

    @ApiModelProperty(value = "发送人昵称")
    private String sendUserNickName;

    @ApiModelProperty(value = "发送时间")
    private LocalDateTime sendTime;

    @ApiModelProperty(value = "接收人ID")
    private String contactId;

    @ApiModelProperty(value = "联系人类型 0:单聊 1:群聊")
    private UserContactType contactType;

    @ApiModelProperty(value = "文件大小")
    private Long fileSize;

    @ApiModelProperty(value = "文件名")
    private String fileName;

    @ApiModelProperty(value = "文件类型")
    private FileType fileType;

    @ApiModelProperty(value = "状态 0:正在发送 1:已发送")
    private MessageStatus status;


}
