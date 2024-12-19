package com.coolchat.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenUserInfoDTO implements Serializable {

    private String token;
    private String userId;
    private String nickName;
    private Boolean admin;
}
