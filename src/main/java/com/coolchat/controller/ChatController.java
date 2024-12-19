package com.coolchat.controller;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.http.HttpResponse;
import com.coolchat.annotation.GlobalInterceptor;
import com.coolchat.domain.dto.SendMessageDTO;
import com.coolchat.domain.dto.TokenUserInfoDTO;
import com.coolchat.domain.dto.UploadFileDTO;
import com.coolchat.domain.po.ChatMessage;
import com.coolchat.enums.MessageType;
import com.coolchat.exception.BizIllegalException;
import com.coolchat.result.Result;
import com.coolchat.service.IChatMessageService;
import com.coolchat.util.RedisComponent;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@Slf4j
@Api(tags = "消息相关接口")
public class ChatController {
    private final RedisComponent redisComponent;
    private final IChatMessageService chatMessageService;

    /**
     * 发送消息
     * @param messageDTO
     * @return
     */
    @PostMapping("/sendMessage")
    @ApiOperation("发送消息")
    @GlobalInterceptor
    public Result sendMessage(HttpServletRequest request, SendMessageDTO messageDTO){

        TokenUserInfoDTO tokenUserInfoDTO = redisComponent.getTokenUserInfoDTO(request);
        return chatMessageService.sendMessage(tokenUserInfoDTO, messageDTO);
    }

    /**
     * 上传文件
     * @param request
     * @param uploadFileDTO
     * @return
     */
    @PostMapping("/uploadFile")
    @ApiOperation("上传文件")
    @GlobalInterceptor
    public Result uploadFile(HttpServletRequest request, UploadFileDTO uploadFileDTO){
        TokenUserInfoDTO tokenUserInfoDTO = redisComponent.getTokenUserInfoDTO(request);
        return chatMessageService.uploadFile(tokenUserInfoDTO, uploadFileDTO);
    }


    /**
     * 下载文件
     * @param request
     * @param response
     * @param fileId
     * @param showCover
     * @return
     */
    @PostMapping("/downloadFile")
    @ApiOperation("下载文件")
    @GlobalInterceptor
    public Result downloadFile(HttpServletRequest request, HttpServletResponse response,
                               @NotEmpty String fileId, @NotNull Boolean showCover){
        TokenUserInfoDTO tokenUserInfoDTO = redisComponent.getTokenUserInfoDTO(request);
        return chatMessageService.downloadFile(tokenUserInfoDTO, response, fileId, showCover);
    }
}
