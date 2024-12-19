package com.coolchat.domain.dto;

import com.coolchat.enums.JoinType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

@Data
@Builder
public class UserInfoDTO implements Serializable {

    private String nickName;

    private Integer sex;

    private Integer joinType;

    private String personalSignature;

    private String areaName;

    private String areaCode;

    /**
     头像文件
     */
    private MultipartFile avatarFile;
    /**
     * 头像封面，缩略图
     */
    private MultipartFile avatarCover;
}
