package com.coolchat.service;

import com.coolchat.domain.po.UserContact;
import com.baomidou.mybatisplus.extension.service.IService;
import com.coolchat.domain.vo.UserContactVO;
import com.coolchat.domain.vo.UserInfoVO;
import com.coolchat.enums.UserContactStatus;
import com.coolchat.result.Result;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author yayibai
 * @since 2024-08-20
 */
public interface IUserContactService extends IService<UserContact> {



    /**
     * 搜索联系人
     * @param request
     * @param contactId
     * @return
     */
    Result<UserContactVO> search(HttpServletRequest request, String contactId);

    /**
     * 获取联系人列表
     * @param request
     * @param contactType
     * @return
     */
    Result<List<UserContactVO>> loadContact(HttpServletRequest request, Integer contactType);

    /**
     * 获取联系人详情,不一定是好友
     * @param request
     * @param contactId
     * @return
     */
    Result<UserInfoVO> getContactInfo(HttpServletRequest request, String contactId);

    /**
     * 获取联系人详情,一定是好友
     * @param request
     * @param contactId
     * @return
     */
    Result<UserInfoVO> getContactUserInfo(HttpServletRequest request, String contactId);

    /**
     * 删除或拉黑联系人
     * @param request
     * @param contactId
     * @return
     */
    Result delOrBlockContact(HttpServletRequest request, String contactId, UserContactStatus status);
}
