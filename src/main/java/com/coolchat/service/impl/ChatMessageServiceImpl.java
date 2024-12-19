package com.coolchat.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.coolchat.config.AppConfig;
import com.coolchat.constants.FileConstants;
import com.coolchat.constants.SysSettingConstants;
import com.coolchat.domain.dto.*;
import com.coolchat.domain.po.ChatMessage;
import com.coolchat.domain.po.ChatSession;
import com.coolchat.domain.po.UserContact;
import com.coolchat.enums.*;
import com.coolchat.exception.BizIllegalException;
import com.coolchat.mapper.ChatMessageMapper;
import com.coolchat.mapper.ChatSessionMapper;
import com.coolchat.result.Result;
import com.coolchat.service.IChatMessageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coolchat.util.ChatMessageUtils;
import com.coolchat.util.RedisComponent;
import com.coolchat.websocket.MessageHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author yayibai
 * @since 2024-12-03
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements IChatMessageService {
    private final RedisComponent redisComponent;
    private final ChatSessionMapper chatSessionMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final MessageHandler messageHandler;
    private final AppConfig appConfig;

    /**
     * 发送消息
     *
     * @param tokenUserInfoDTO
     * @param messageDTO
     * @return
     */
    @Override
    public Result sendMessage(TokenUserInfoDTO tokenUserInfoDTO, SendMessageDTO messageDTO) {
        MessageType messageType = MessageType.getByType(messageDTO.getMessageType());
        if (messageType == null || !ArrayUtil.contains(new Integer[]{
                MessageType.CHAT.getType(),
                MessageType.MEDIA_CHAT.getType()
        }, messageType.getType())) {
            throw new BizIllegalException("非法信息");
        }
        FileType fileType = FileType.getByType(messageDTO.getFileType());
        if (fileType == null && MessageType.MEDIA_CHAT.equals(messageType)) {
            throw new BizIllegalException("非法输入");
        }

        ChatMessage chatMessage = ChatMessage.builder()
                .contactId(messageDTO.getContactId())
                .messageContent(messageDTO.getMessageContent())
                .fileSize(messageDTO.getFileSize())
                .fileName(messageDTO.getFileName())
                .fileType(fileType)
                .messageType(messageType)
                .build();
        //判断是否为机器人
        if (SysSettingConstants.ROBOT_UID.equals(messageDTO.getContactId())) {
            return Result.error("对方为机器人");
        }
        List<String> contactIdList = redisComponent.getUserContactIdList(tokenUserInfoDTO.getUserId());
        //判断无法发出消息理由
        if (!contactIdList.contains(chatMessage.getContactId())) {
            UserContactType contactType = UserContactType.getById(chatMessage.getContactId());
            if (UserContactType.USER.equals(contactType)) {
                throw new BizIllegalException("请先添加对方为好友");
            } else if (UserContactType.Group.equals(contactType)) {
                throw new BizIllegalException("请先添加群组");
            }
        }

        String sessionId = null;
        String sendUserId = tokenUserInfoDTO.getUserId();
        String contactId = chatMessage.getContactId();
        UserContactType contactType = UserContactType.getById(contactId);

        if (UserContactType.USER.equals(contactType)) {
            sessionId = ChatMessageUtils.getChatSessionId4User(new String[]{sendUserId, contactId});
        } else if (UserContactType.Group.equals(contactType)) {
            sessionId = ChatMessageUtils.getChatSessionId4Group(contactId);
        } else {
            throw new BizIllegalException("非法输入");
        }

        MessageStatus status = messageType == MessageType.MEDIA_CHAT ? MessageStatus.SENDING : MessageStatus.SENT;

        String messageContent = ChatMessageUtils.cleanHtmlTag(chatMessage.getMessageContent());

        chatMessage.setSessionId(sessionId);
        chatMessage.setSendTime(LocalDateTime.now());
        chatMessage.setStatus(status);
        chatMessage.setMessageContent(messageContent);

        //更新会话
        ChatSession chatSession = ChatSession.builder()
                .sessionId(chatMessage.getSessionId())
                .lastMessage(messageContent)
                .lastReceiveTime(LocalDateTime.now())
                .build();
        if (UserContactType.Group.equals(contactType)) {
            chatSession.setLastMessage(tokenUserInfoDTO.getNickName() + ":" + messageContent);
        }

        //update or insert
        ChatSession dbSession = chatSessionMapper.selectById(chatSession);
        if (dbSession == null){
            chatSessionMapper.insert(chatSession);
        }else {
            chatSessionMapper.updateById(chatSession);
        }

        //记录消息表
        chatMessage.setSendUserId(sendUserId);
        chatMessage.setSendUserNickName(tokenUserInfoDTO.getNickName());
        chatMessage.setContactType(contactType);
        chatMessageMapper.insert(chatMessage);
        MessageSendDTO messageSendDTO = BeanUtil.copyProperties(chatMessage, MessageSendDTO.class);

        //如果接收者为机器人
        if (SysSettingConstants.ROBOT_UID.equals(contactId)) {
            SysSettingDTO sysSettingDTO = redisComponent.getSysSetting();
            TokenUserInfoDTO robot = TokenUserInfoDTO.builder()
                    .userId(sysSettingDTO.getRobotUid())
                    .nickName(sysSettingDTO.getRobotNickName())
                    .build();
            SendMessageDTO robotChatMessage = SendMessageDTO.builder()
                    .contactId(sendUserId)
                    //TODO: 调用ai接口
                    .messageContent("我只是一个机器人")
                    .messageType(MessageType.CHAT.getType())
                    .build();
            sendMessage(robot, robotChatMessage);
        } else {
            messageHandler.sendMessage(messageSendDTO);
        }

        return Result.success();
    }

    /**
     * 上传文件
     *
     * @param tokenUserInfoDTO
     * @param uploadFileDTO
     * @return
     */
    @Override
    public Result uploadFile(TokenUserInfoDTO tokenUserInfoDTO, UploadFileDTO uploadFileDTO) {

        String userId = tokenUserInfoDTO.getUserId();
        Long messageId = uploadFileDTO.getMessageId();
        MultipartFile file = uploadFileDTO.getFile();
        MultipartFile cover = uploadFileDTO.getCover();
        String filename = file.getOriginalFilename();

        ChatMessage chatMessage = chatMessageMapper.selectById(messageId);
        if (chatMessage == null || !chatMessage.getSendUserId().equals(userId)) {
            throw new BizIllegalException("参数异常");
        }

        SysSettingDTO sysSettingDTO = redisComponent.getSysSetting();
        String fileSuffix = ChatMessageUtils.getFileSuffix(filename);

        //用参数正常的否定得出参数异常
        if (!(!StrUtil.isEmpty(fileSuffix)
                && (ArrayUtil.contains(FileConstants.IMAGE_SUFFIX_LIST, fileSuffix.toLowerCase()) && file.getSize() < sysSettingDTO.getMaxFileSize() * FileConstants.FILE_SIZE_MB
                || ArrayUtil.contains(FileConstants.VIDEO_SUFFIX_LIST, fileSuffix.toLowerCase()) && file.getSize() < sysSettingDTO.getMaxVideoSize() * FileConstants.FILE_SIZE_MB))){
            throw new BizIllegalException("参数异常");
        }

        String fileRealName = messageId + fileSuffix;
        String month = DateUtil.format(chatMessage.getSendTime(), "yyyyMM");
        File folder = new File(appConfig.getProjectFolder() + FileConstants.FILE_FOLDER + month);
        if (!folder.exists()){
            folder.mkdirs();
        }

        File uploadFile = new File(folder.getPath() + "/" + fileRealName);

        try {
            file.transferTo(uploadFile);
            cover.transferTo(new File(uploadFile.getPath() + FileConstants.COVER_IMAGE_SUFFIX));
        } catch (IOException e) {
            log.error("上传文件失败", e);
            throw new BizIllegalException("文件上传失败");
        }

        lambdaUpdate().eq(ChatMessage::getMessageId, messageId)
                .eq(ChatMessage::getStatus, MessageStatus.SENDING)
                .set(ChatMessage::getStatus, MessageStatus.SENT)
                .update();

        MessageSendDTO<Object> messageSendDTO = MessageSendDTO.builder()
                .status(MessageStatus.SENT)
                .messageId(messageId)
                .messageType(MessageType.FILE_UPDATE)
                .contactId(chatMessage.getContactId())
                .build();
        messageHandler.sendMessage(messageSendDTO);

        return Result.success();
    }

    /**
     * 下载文件
     * @param tokenUserInfoDTO
     * @param response
     * @param fileId
     * @param showCover
     * @return
     */
    @Override
    public Result downloadFile(TokenUserInfoDTO tokenUserInfoDTO, HttpServletResponse response, String fileId, Boolean showCover) {

        OutputStream out = null;
        FileInputStream in = null;

        try {
            File file = null;
            if (!NumberUtil.isNumber(fileId)) {
                String avatarFolderName = FileConstants.FILE_FOLDER + FileConstants.AVATAR_FOLDER;
                String avatarPath = appConfig.getProjectFolder() + avatarFolderName + fileId + FileConstants.IMAGE_SUFFIX;
                if (showCover != null && showCover) {
                    avatarPath = avatarPath + FileConstants.COVER_IMAGE_SUFFIX;
                }
                file = new File(avatarPath);
                if (!file.exists()) {
                    throw new BizIllegalException("文件不存在");
                }
            }else {
                file = download(tokenUserInfoDTO, Long.parseLong(fileId), showCover);
            }

            //netty byteBuf
            response.setContentType("application/x-msdownload;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment");
            response.setContentLengthLong(file.length());
            in = new FileInputStream(file);
            byte[] byteData = new byte[1024];
            out = response.getOutputStream();
            int len;
            while ((len = in.read(byteData)) != -1){
                out.write(byteData, 0, len);
            }
            out.flush();

        } catch (Exception e) {
            log.error("下载文件失败", e);
        }finally {
            if (out != null){
                try{
                    out.close();
                }catch (Exception e){
                    log.error("IO异常", e);
                }
            }
            if (in != null){
                try{
                    in.close();
                }catch (Exception e){
                    log.error("IO异常", e);
                }
            }
        }

        return Result.success();
    }

    private File download(TokenUserInfoDTO userInfoDTO, Long messageId, Boolean showCover){
        ChatMessage message = chatMessageMapper.selectById(messageId);
        String contactId = message.getContactId();
        UserContactType contactType = UserContactType.getById(contactId);
        if (UserContactType.USER.equals(contactType) && !userInfoDTO.getUserId().equals(message.getContactId())){
            throw new BizIllegalException("参数错误");
        }

        if (UserContactType.Group.equals(contactType)){
            Long contactCount = Db.lambdaQuery(UserContact.class)
                    .eq(UserContact::getUserId, userInfoDTO.getUserId())
                    .eq(UserContact::getContactType, UserContactType.Group)
                    .eq(UserContact::getContactId, contactId)
                    .eq(UserContact::getStatus, UserContactStatus.FRIEND)
                    .count();
            if (contactCount == 0){
                throw new BizIllegalException("参数错误");
            }
        }

        String month = DateUtil.format(message.getSendTime(), "yyyyMM");
        File folder = new File(appConfig.getProjectFolder() + FileConstants.FILE_FOLDER + month);
        if (!folder.exists()){
            folder.mkdirs();
        }
        String fileName = message.getFileName();
        String fileExtName = ChatMessageUtils.getFileSuffix(fileName);
        String fileReadName = messageId + fileExtName;
        if (showCover != null && showCover){
            fileReadName = fileReadName + FileConstants.COVER_IMAGE_SUFFIX;
        }
        File file = new File(folder.getPath() + "/" + fileReadName);
        if (!file.exists()){
            log.error("文件不存在{}", messageId);
            throw new BizIllegalException("文件不存在");
        }

        return file;
    }
}
