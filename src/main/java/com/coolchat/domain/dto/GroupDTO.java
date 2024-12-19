package com.coolchat.domain.dto;

import com.coolchat.enums.JoinType;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class GroupDTO implements Serializable {
    private String groupId;
    @NotEmpty
    private String groupName;
    private String groupNotice;
    @NotNull
    private Integer joinType;
    /**
    头像文件
     */
    private MultipartFile avatarFile;
    /**
     * 头像封面，缩略图
     */
    private MultipartFile avatarCover;
}
