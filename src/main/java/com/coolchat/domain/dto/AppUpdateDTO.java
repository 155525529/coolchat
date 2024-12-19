package com.coolchat.domain.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class AppUpdateDTO implements Serializable {
    private Integer id;
    @NotEmpty
    private String version;
    @NotEmpty
    private String updateDesc;
    @NotNull
    private Integer fileType;
    private String outLink;
    private MultipartFile file;
}

