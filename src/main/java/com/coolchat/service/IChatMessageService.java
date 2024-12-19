package com.coolchat.service;

import cn.hutool.http.HttpResponse;
import com.coolchat.domain.dto.SendMessageDTO;
import com.coolchat.domain.dto.TokenUserInfoDTO;
import com.coolchat.domain.dto.UploadFileDTO;
import com.coolchat.domain.po.ChatMessage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.coolchat.result.Result;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author yayibai
 * @since 2024-12-03
 */
public interface IChatMessageService extends IService<ChatMessage> {

    /**
     * 发送消息
     * @param tokenUserInfoDTO
     * @param messageDTO
     * @return
     */
    Result sendMessage(TokenUserInfoDTO tokenUserInfoDTO, SendMessageDTO messageDTO);

    /**
     * 上传文件
     * @param tokenUserInfoDTO
     * @param uploadFileDTO
     * @return
     */
    Result uploadFile(TokenUserInfoDTO tokenUserInfoDTO, UploadFileDTO uploadFileDTO);

    /**
     * 下载文件
     * @param tokenUserInfoDTO
     * @param response
     * @param fileId
     * @param showCover
     * @return
     */
    Result downloadFile(TokenUserInfoDTO tokenUserInfoDTO, HttpServletResponse response, String fileId, Boolean showCover);
}
