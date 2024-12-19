package com.coolchat.domain.vo;

import com.coolchat.enums.JoinType;
import com.coolchat.enums.UserContactStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoVO implements Serializable {
    private String userId;

    private String nickName;

    private JoinType joinType;

    private Integer sex;

    private String personalSignature;

    private String token;

    private String areaName;

    private String areaCode;

    private Boolean admin;

    private UserContactStatus contactStatus;
}
