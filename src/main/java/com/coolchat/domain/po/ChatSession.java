package com.coolchat.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
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
@TableName("chat_session")
@ApiModel(value="ChatSession对象", description="")
@Builder
public class ChatSession implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "会话id")
    @TableId(value = "session_id", type = IdType.INPUT)
    private String sessionId;

    @ApiModelProperty(value = "最后接收的消息")
    private String lastMessage;

    @ApiModelProperty(value = "最后接收消息时间")
    private LocalDateTime lastReceiveTime;


}
