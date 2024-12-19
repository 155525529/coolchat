package com.coolchat.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ArrayUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.coolchat.domain.dto.TokenUserInfoDTO;
import com.coolchat.domain.po.GroupInfo;
import com.coolchat.domain.po.UserContact;
import com.coolchat.domain.po.UserInfo;
import com.coolchat.domain.vo.UserContactVO;
import com.coolchat.domain.vo.UserInfoVO;
import com.coolchat.enums.*;
import com.coolchat.exception.BizIllegalException;
import com.coolchat.mapper.GroupInfoMapper;
import com.coolchat.mapper.UserContactApplyMapper;
import com.coolchat.mapper.UserContactMapper;
import com.coolchat.mapper.UserInfoMapper;
import com.coolchat.result.Result;
import com.coolchat.service.IUserContactService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coolchat.util.RedisComponent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author yayibai
 * @since 2024-08-20
 */
@Service
@RequiredArgsConstructor
public class UserContactServiceImpl extends ServiceImpl<UserContactMapper, UserContact> implements IUserContactService {
    private final UserInfoMapper userInfoMapper;
    private final GroupInfoMapper groupInfoMapper;
    private final UserContactMapper userContactMapper;
    private final RedisComponent redisComponent;
    private final UserContactApplyMapper userContactApplyMapper;

    /**
     * 搜索联系人
     *
     * @param request
     * @param contactId
     * @return
     */
    @Override
    public Result<UserContactVO> search(HttpServletRequest request, String contactId) {
        TokenUserInfoDTO tokenUserInfoDTO = redisComponent.getTokenUserInfoDTO(request);
        //检查输入前缀是否正确
        UserContactType contactType = UserContactType.getById(contactId);
        if (contactType == null) {
            return null;
        }
        //获取返回所需值
        UserContactVO userContactVO = new UserContactVO();
        if (contactType == UserContactType.USER) {
            UserInfo userInfo = userInfoMapper.selectById(contactId);
            if (userInfo == null) {
                return null;
            }
            userContactVO.setName(userInfo.getNickName());
            userContactVO.setUserStatus(userInfo.getStatus());
            userContactVO.setSex(userInfo.getSex());
            userContactVO.setAreaName(userInfo.getAreaName());
        } else {
            GroupInfo groupInfo = groupInfoMapper.selectById(contactId);
            if (groupInfo == null) {
                return null;
            }
            userContactVO.setName(groupInfo.getGroupName());
        }
        userContactVO.setContactId(contactId);
        userContactVO.setContactType(contactType);

        //搜的是自己
        if (tokenUserInfoDTO.getUserId().equals(contactId)) {
            userContactVO.setContactStatus(UserContactStatus.FRIEND);
            return Result.success(userContactVO);
        }

        //查询是否是好友
        UserContact contact = userContactMapper.getByUserIdAndContactId(tokenUserInfoDTO.getUserId(), contactId);
        userContactVO.setContactStatus(contact == null ? null : contact.getStatus());

        return Result.success(userContactVO);
    }

    /**
     * 获取联系人列表
     *
     * @param request
     * @param contactType
     * @return
     */
    @Override
    public Result<List<UserContactVO>> loadContact(HttpServletRequest request, Integer contactType) {
        TokenUserInfoDTO tokenUserInfoDTO = redisComponent.getTokenUserInfoDTO(request);
        UserContactType type = UserContactType.getByNum(contactType);
        List<UserContactVO> list;
        if (UserContactType.USER.equals(type)) {
            list = userContactMapper.getContactOfFriendList(tokenUserInfoDTO.getUserId());
        } else {
            list = userContactMapper.getContactOfGroupList(tokenUserInfoDTO.getUserId());
        }

        return Result.success(list);
    }

    /**
     * 获取联系人详情,不一定是好友
     *
     * @param request
     * @param contactId
     * @return
     */
    @Override
    public Result<UserInfoVO> getContactInfo(HttpServletRequest request, String contactId) {
        TokenUserInfoDTO tokenUserInfoDTO = redisComponent.getTokenUserInfoDTO(request);
        UserInfo userInfo = userInfoMapper.selectById(contactId);
        UserInfoVO userInfoVO = BeanUtil.copyProperties(userInfo, UserInfoVO.class);
        userInfoVO.setContactStatus(UserContactStatus.NOT_FRIEND);
        UserContact contact = userContactMapper.getByUserIdAndContactId(tokenUserInfoDTO.getUserId(), contactId);
        if (contact != null) {
            userInfoVO.setContactStatus(UserContactStatus.FRIEND);
        }

        return Result.success(userInfoVO);
    }

    /**
     * 获取联系人详情,一定是好友
     *
     * @param request
     * @param contactId
     * @return
     */
    @Override
    public Result<UserInfoVO> getContactUserInfo(HttpServletRequest request, String contactId) {
        TokenUserInfoDTO tokenUserInfoDTO = redisComponent.getTokenUserInfoDTO(request);
        UserContact contact = userContactMapper.getByUserIdAndContactId(tokenUserInfoDTO.getUserId(), contactId);
        if (contact == null || !ArrayUtil.contains(new Integer[]{
                UserContactStatus.FRIEND.getStatus(),
                UserContactStatus.BE_DEL.getStatus(),
                UserContactStatus.BE_BLOCK.getStatus(),
        }, contact.getStatus().getStatus())) {
            throw new BizIllegalException("非法请求");
        }
        UserInfo userInfo = userInfoMapper.selectById(contactId);
        UserInfoVO userInfoVO = BeanUtil.copyProperties(userInfo, UserInfoVO.class);
        userInfoVO.setContactStatus(UserContactStatus.NOT_FRIEND);

        return Result.success(userInfoVO);
    }

    /**
     * 删除或拉黑联系人
     *
     * @param request
     * @param contactId
     * @return
     */
    @Override
    public Result delOrBlockContact(HttpServletRequest request, String contactId, UserContactStatus status) {
        TokenUserInfoDTO tokenUserInfoDTO = redisComponent.getTokenUserInfoDTO(request);
        String userId = tokenUserInfoDTO.getUserId();
        UserContact contact = lambdaQuery().eq(UserContact::getContactId, contactId).one();
        if (contact == null) {
            throw new BizIllegalException("操作失败");
        }

        LambdaUpdateWrapper<UserContact> wrapper1 = new LambdaUpdateWrapper<>();
        wrapper1.eq(UserContact::getUserId, userId)
                .eq(UserContact::getContactId, contactId);
        LambdaUpdateWrapper<UserContact> wrapper2 = new LambdaUpdateWrapper<>();
        wrapper2.eq(UserContact::getUserId, contactId)
                .eq(UserContact::getContactId, userId);

        if (UserContactStatus.DEL.equals(status)) {
            //删除好友
            wrapper1.set(UserContact::getStatus, UserContactStatus.DEL);
            wrapper2.set(UserContact::getStatus, UserContactStatus.BE_DEL);
        } else if (UserContactStatus.BLOCK.equals(status)) {
            //拉黑好友
            wrapper1.set(UserContact::getStatus, UserContactStatus.BLOCK);
            wrapper2.set(UserContact::getStatus, UserContactStatus.BE_BLOCK);
        }else {
            throw new BizIllegalException("操作失败");
        }

        userContactMapper.update(null, wrapper1);
        userContactMapper.update(null, wrapper2);

        //从我的好友列表缓存中删除好友
        redisComponent.removeUserContact(userId, contactId);

        //从好友列表缓存中删除我
        redisComponent.removeUserContact(contactId, userId);

        return Result.success();
    }
}