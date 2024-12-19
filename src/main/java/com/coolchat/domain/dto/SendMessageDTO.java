package com.coolchat.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageDTO implements Serializable {
    @NotEmpty
    private String contactId;
    @NotEmpty
    @Max(500)
    private String messageContent;
    @NotNull
    private Integer messageType;
    private Long fileSize;
    private String fileName;
    private Integer fileType;
}
