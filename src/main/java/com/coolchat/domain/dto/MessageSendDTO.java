package com.coolchat.domain.dto;

import com.coolchat.enums.MessageStatus;
import com.coolchat.enums.MessageType;
import com.coolchat.enums.UserContactType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class MessageSendDTO<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long messageId;

    private String sessionId;

    private MessageType messageType;

    private String messageContent;

    private String sendUserId;

    private String sendUserNickName;

    private Long sendTime;

    private String contactId;

    private String contactName;

    //联系人类型 0:单聊 1:群聊
    private UserContactType contactType;

    private Long fileSize;

    private String fileName;

    private Boolean fileType;

    private MessageStatus status;

    private String lastMessage = messageContent;

    private Integer memberCount;

    private T extendData;

}
