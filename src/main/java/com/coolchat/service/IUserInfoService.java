package com.coolchat.service;

import com.coolchat.domain.dto.*;
import com.coolchat.domain.po.UserInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.coolchat.domain.query.PageQuery;
import com.coolchat.domain.vo.UserInfoVO;
import com.coolchat.result.Result;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author yayibai
 * @since 2024-08-13
 */
public interface IUserInfoService extends IService<UserInfo> {

    /**
     * 注册
     * @param loginDTO
     */
    Result register(RegisterDTO loginDTO);

    /**
     * 登录
     * @param loginDTO
     * @return
     */
    Result login(LoginDTO loginDTO);

    /**
     * 生成验证码
     * @return
     */
    Result<HashMap<String, String>> checkCode();

    /**
     * 获取用户信息
     * @param request
     * @return
     */
    Result<UserInfoVO> getUserInfo(HttpServletRequest request);

    /**
     * 更新用户信息
     * @param request
     * @param userInfoDTO
     * @return
     */
    Result updateUserInfo(HttpServletRequest request, UserInfoDTO userInfoDTO) throws IOException;

    /**
     * 修改密码
     * @param request
     * @param password
     * @return
     */
    Result updatePassword(HttpServletRequest request, String password);

    /**
     * 获取用户列表
     * @param query
     * @return
     */
    Result<PageDTO<UserInfoVO>> loadUser(PageQuery query);

    /**
     * 更新用户状态
     * @param status
     * @param userId
     * @return
     */
    Result updateUserStatus(Integer status, String userId);

    /**
     * 强制下线
     * @param userId
     * @return
     */
    Result forceOffLine(String userId);


}
