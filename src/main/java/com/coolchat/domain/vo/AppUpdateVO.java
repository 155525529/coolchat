package com.coolchat.domain.vo;

import com.coolchat.enums.FileType;
import lombok.Data;

@Data
public class AppUpdateVO {
    private Integer id;
    private String version;
    private String updateDesc;
    private Long size;
    private String fileName;
    private FileType fileType;
    private String outerLink;
}
