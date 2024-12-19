package com.coolchat.controller;


import com.coolchat.annotation.GlobalInterceptor;
import com.coolchat.domain.dto.PageDTO;
import com.coolchat.domain.query.PageQuery;
import com.coolchat.domain.vo.ApplySearchVO;
import com.coolchat.domain.vo.UserContactVO;
import com.coolchat.domain.vo.UserInfoVO;
import com.coolchat.enums.UserContactStatus;
import com.coolchat.result.Result;
import com.coolchat.service.IUserContactApplyService;
import com.coolchat.service.IUserContactService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author yayibai
 * @since 2024-08-20
 */
@RestController
@RequestMapping("/contact")
@RequiredArgsConstructor
@Slf4j
@Api(tags = "联系人相关接口")
public class UserContactController {
    private final IUserContactService userContactService;
    private final IUserContactApplyService userContactApplyService;
    /**
     * 搜索联系人
     * @param request
     * @param contactId
     * @return
     */
    @GetMapping("/search")
    @ApiOperation("搜索联系人")
    @GlobalInterceptor
    public Result<UserContactVO> search(HttpServletRequest request, String contactId) {
        return userContactService.search(request, contactId);
    }

    /**
     * 申请添加联系人
     * @param request
     * @param contactId
     * @param applyInfo
     * @return
     */
    @PostMapping("/applyAdd")
    @ApiOperation("申请添加联系人")
    @GlobalInterceptor
    public Result applyAdd(HttpServletRequest request, String contactId, String applyInfo){
        return userContactApplyService.applyAdd(request, contactId, applyInfo);
    }

    /**
     * 获取好友申请列表
     * @param request
     * @param query
     * @return
     */
    @GetMapping("/loadApply")
    @ApiOperation("获取好友申请列表")
    @GlobalInterceptor
    public Result<PageDTO<ApplySearchVO>> loadApply(HttpServletRequest request, PageQuery query){
        return userContactApplyService.loadApply(request, query);
    }

    /**
     * 处理联系人申请
     * @param request
     * @param applyId
     * @param status
     * @return
     */
    @PostMapping("/dealWithApply")
    @ApiOperation("处理联系人申请")
    @GlobalInterceptor
    public Result dealWithApply(HttpServletRequest request, Integer applyId, Integer status){
        return userContactApplyService.dealWithApply(request, applyId, status);
    }

    /**
     * 获取联系人列表
     * @param request
     * @param contactType
     * @return
     */
    @GetMapping("/loadContact")
    @ApiOperation("获取联系人列表")
    @GlobalInterceptor
    public Result<List<UserContactVO>> loadContact(HttpServletRequest request, Integer contactType){
        return userContactService.loadContact(request, contactType);
    }

    /**
     * 获取联系人详情,不一定是好友
     * @param request
     * @param contactId
     * @return
     */
    @GetMapping("/getContactInfo")
    @ApiOperation("获取联系人详情")
    @GlobalInterceptor
    public Result<UserInfoVO> getContactInfo(HttpServletRequest request, String contactId){
        return userContactService.getContactInfo(request, contactId);
    }

    /**
     * 获取联系人详情,一定是好友
     * @param request
     * @param contactId
     * @return
     */
    @GetMapping("/getContactUserInfo")
    @ApiOperation("获取好友详情")
    @GlobalInterceptor
    public Result<UserInfoVO> getContactUserInfo(HttpServletRequest request, String contactId){
        return userContactService.getContactUserInfo(request, contactId);
    }

    /**
     * 删除联系人
     * @param request
     * @param contactId
     * @return
     */
    @DeleteMapping("/delContact")
    @ApiOperation("删除联系人")
    @GlobalInterceptor
    public Result delContact(HttpServletRequest request, String contactId){
        return userContactService.delOrBlockContact(request, contactId, UserContactStatus.DEL);
    }

    /**
     * 拉黑联系人
     * @param request
     * @param contactId
     * @return
     */
    @PostMapping("/blockContact")
    @ApiOperation("拉黑联系人")
    @GlobalInterceptor
    public Result blockContact(HttpServletRequest request, String contactId){
        return userContactService.delOrBlockContact(request, contactId, UserContactStatus.BLOCK);
    }
}
