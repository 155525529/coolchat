package com.coolchat.domain.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class LoginDTO implements Serializable {
    private String checkCodeKey;
    private String email;
    private String password;
    private String checkCode;
}
