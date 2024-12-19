package com.coolchat.domain.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class UploadFileDTO implements Serializable {
    @NotNull
    private Long messageId;
    @NotNull
    private MultipartFile file;
    @NotNull
    private MultipartFile cover;
}
