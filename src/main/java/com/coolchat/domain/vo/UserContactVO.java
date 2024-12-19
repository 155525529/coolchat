package com.coolchat.domain.vo;

import com.coolchat.enums.UserContactStatus;
import com.coolchat.enums.UserContactType;
import com.coolchat.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserContactVO implements Serializable {
    private String contactId;
    private UserContactType contactType;
    private UserContactStatus contactStatus;
    private String Name;
    private UserStatus userStatus;
    private Integer sex;
    private String areaName;
}
