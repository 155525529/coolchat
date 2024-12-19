package com.coolchat.domain.dto;

import com.coolchat.domain.po.ChatMessage;
import com.coolchat.domain.vo.ChatSessionUserVO;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class WsInitDataDTO implements Serializable {
    private List<ChatSessionUserVO> chatSessionList;
    private List<ChatMessage> chatMessageList;
    private Integer applyCount;
}
