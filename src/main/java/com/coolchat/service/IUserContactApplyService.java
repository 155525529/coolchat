package com.coolchat.service;

import com.coolchat.domain.dto.PageDTO;
import com.coolchat.domain.po.UserContactApply;
import com.baomidou.mybatisplus.extension.service.IService;
import com.coolchat.domain.query.PageQuery;
import com.coolchat.domain.vo.ApplySearchVO;
import com.coolchat.result.Result;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author yayibai
 * @since 2024-08-20
 */
public interface IUserContactApplyService extends IService<UserContactApply> {

    /**
     * 申请添加联系人
     * @param request
     * @param contactId
     * @param applyInfo
     * @return
     */
    Result applyAdd(HttpServletRequest request, String contactId, String applyInfo);

    /**
     * 获取好友申请列表
     *
     * @param request
     * @param query
     * @return
     */
    Result<PageDTO<ApplySearchVO>> loadApply(HttpServletRequest request, PageQuery query);

    /**
     * 处理联系人申请
     * @param request
     * @param applyId
     * @param status
     * @return
     */
    Result dealWithApply(HttpServletRequest request, Integer applyId, Integer status);

}
