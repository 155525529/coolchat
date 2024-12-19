package com.coolchat.controller;


import com.coolchat.annotation.GlobalInterceptor;
import com.coolchat.domain.dto.GroupDTO;
import com.coolchat.domain.dto.TokenUserInfoDTO;
import com.coolchat.domain.po.GroupInfo;
import com.coolchat.domain.vo.GroupInfoVO;
import com.coolchat.enums.MessageType;
import com.coolchat.result.Result;
import com.coolchat.service.IGroupInfoService;
import com.coolchat.service.IUserContactService;
import com.coolchat.util.RedisComponent;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author yayibai
 * @since 2024-08-20
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "群组相关接口")
@RequestMapping("/group")
public class GroupInfoController {
    private final RedisComponent redisComponent;
    private final IGroupInfoService groupInfoService;
    private final RedisTemplate redisTemplate;
    private final IUserContactService userContactService;

    /**
     * 创建群组
     * @param groupDTO
     * @return
     */
    @PostMapping("/saveGroup")
    @ApiOperation("创建群组")
    @GlobalInterceptor
    public Result saveGroup(GroupDTO groupDTO, HttpServletRequest request) throws IOException {

        return groupInfoService.saveGroup(groupDTO, request);
    }

    /**
     * 获取我创建的群组
     * @param request
     * @return
     */
    @GetMapping("/loadMyGroup")
    @ApiOperation("获取我创建的群组")
    @GlobalInterceptor
    public Result<List<GroupInfo>> loadMyGroup(HttpServletRequest request){
        TokenUserInfoDTO tokenUserInfoDTO = redisComponent.getTokenUserInfoDTO(request);
        List<GroupInfo> list = groupInfoService.lambdaQuery().eq(GroupInfo::getGroupOwnerId, tokenUserInfoDTO.getUserId()).list();
        return Result.success(list);

    }

    /**
     * 获取群组详情
     * @param request
     * @param groupId
     * @return
     */
    @GetMapping("/getGroupInfo")
    @ApiOperation("获取群组详情")
    @GlobalInterceptor
    public Result<GroupInfo> getGroupInfo(HttpServletRequest request, String groupId){

        return groupInfoService.getGroupInfo(request, groupId);
    }

    /**
     * 获取聊天会话群聊详情
     * @param request
     * @param groupId
     * @return
     */
    @GetMapping("/getGroupInfo4Chat")
    @ApiOperation("获取聊天会话群聊详情")
    @GlobalInterceptor
    public Result<GroupInfoVO> getGroupInfo4Chat(HttpServletRequest request, @NotEmpty String groupId){

        return groupInfoService.getGroupInfo4Chat(request, groupId);
    }

    /**
     * 添加或移除群成员
     * @param request
     * @param groupId
     * @param selectContacts
     * @param opType
     * @return
     */
    @GetMapping("/addOrRemoveGroupUser")
    @ApiOperation("添加或移除群成员")
    @GlobalInterceptor
    public Result addOrRemoveGroupUser(HttpServletRequest request,
                                                    @NotEmpty String groupId,
                                                    @NotEmpty String selectContacts,
                                                    @NotNull Integer opType){

        TokenUserInfoDTO tokenUserInfoDTO = redisComponent.getTokenUserInfoDTO(request);
        return groupInfoService.addOrRemoveGroupUser(tokenUserInfoDTO, groupId, selectContacts, opType);
    }

    /**
     * 退出群聊
     * @param request
     * @param groupId
     * @return
     */
    @GetMapping("/leaveGroup")
    @ApiOperation("退出群聊")
    @GlobalInterceptor
    public Result leaveGroup(HttpServletRequest request, @NotEmpty String groupId){
        TokenUserInfoDTO tokenUserInfoDTO = redisComponent.getTokenUserInfoDTO(request);
        return groupInfoService.leaveGroup(tokenUserInfoDTO.getUserId(), groupId, MessageType.LEAVE_GROUP);
    }

    /**
     * 解散群聊
     * @param request
     * @param groupId
     * @return
     */
    @GetMapping("/dissolutionGroup")
    @ApiOperation("解散群聊")
    @GlobalInterceptor
    public Result dissolutionGroup(HttpServletRequest request, @NotEmpty String groupId){
        TokenUserInfoDTO tokenUserInfoDTO = redisComponent.getTokenUserInfoDTO(request);
        return groupInfoService.dissolutionGroup(groupId, tokenUserInfoDTO, false);
    }
}
