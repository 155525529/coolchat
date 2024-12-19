package com.coolchat.domain.bo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
public class ChatSessionBO implements Serializable {
    private String sessionId;
    private String userId;
    private String contactId;
    private String contactName;
    private String lastMessage;
    private LocalDateTime lastReceiveTime;
    private Integer memberCount;

}
