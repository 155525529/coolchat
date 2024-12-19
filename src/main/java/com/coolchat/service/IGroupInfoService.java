package com.coolchat.service;

import com.coolchat.domain.dto.GroupDTO;
import com.coolchat.domain.dto.TokenUserInfoDTO;
import com.coolchat.domain.po.GroupInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.coolchat.domain.vo.GroupInfoVO;
import com.coolchat.enums.MessageType;
import com.coolchat.result.Result;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author yayibai
 * @since 2024-08-20
 */
public interface IGroupInfoService extends IService<GroupInfo> {

    /**
     * 创建群组
     * @return
     */
    Result saveGroup(GroupDTO groupDTO, HttpServletRequest request) throws IOException;

    /**
     * 获取群组详情
     * @param request
     * @param groupId
     * @return
     */
    Result<GroupInfo> getGroupInfo(HttpServletRequest request, String groupId);

    /**
     * 获取聊天会话群聊详情
     * @param request
     * @param groupId
     * @return
     */
    Result<GroupInfoVO> getGroupInfo4Chat(HttpServletRequest request, String groupId);

    /**
     * 解散群聊
     * @param groupId
     * @return
     */
    Result dissolutionGroup(String groupId, TokenUserInfoDTO tokenUserInfoDTO, Boolean isAdmin);

    /**
     * 添加或移除群成员
     * @param tokenUserInfoDTO
     * @param groupId
     * @param selectContacts
     * @param opType
     * @return
     */
    Result addOrRemoveGroupUser(TokenUserInfoDTO tokenUserInfoDTO, String groupId, String selectContacts, Integer opType);

    /**
     * 退出群聊
     * @param userId
     * @param groupId
     * @param messageType
     * @return
     */
    Result leaveGroup(String userId, String groupId, MessageType messageType);
}
