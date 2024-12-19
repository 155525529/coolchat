package com.coolchat.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatSessionUserVO {
    private String userId;

    private String contactId;

    private String sessionId;

    private String contactName;

    private String lastMessage;

    private LocalDateTime lastReceiveTime;

    private Integer memberCount;
}
