package com.coolchat.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.coolchat.config.AppConfig;
import com.coolchat.constants.FileConstants;
import com.coolchat.constants.JwtClaimsConstant;
import com.coolchat.domain.dto.*;
import com.coolchat.domain.po.*;
import com.coolchat.domain.query.PageQuery;
import com.coolchat.domain.vo.UserInfoVO;
import com.coolchat.enums.*;
import com.coolchat.exception.BadRequestException;
import com.coolchat.exception.BizIllegalException;
import com.coolchat.exception.ForbiddenException;
import com.coolchat.mapper.*;
import com.coolchat.properties.JwtProperties;
import com.coolchat.result.Result;
import com.coolchat.service.IUserInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coolchat.util.ChatMessageUtils;
import com.coolchat.util.JwtUtil;
import com.coolchat.util.Md5Utils;
import com.coolchat.util.RedisComponent;
import com.coolchat.websocket.ChannelContextUtils;
import com.coolchat.websocket.MessageHandler;
import com.wf.captcha.ArithmeticCaptcha;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.coolchat.constants.RedisConstants.CHECK_CODE_KEY;
import static com.coolchat.constants.RedisConstants.CHECK_CODE_TTL;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author yayibai
 * @since 2024-08-13
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements IUserInfoService {

    private final UserInfoMapper userInfoMapper;
    private final RedisTemplate redisTemplate;
    private final AppConfig appConfig;
    private final JwtProperties jwtProperties;
    private final RedisComponent redisComponent;
    private final UserContactMapper userContactMapper;
    private final ChatSessionMapper chatSessionMapper;
    private final ChatSessionUserMapper chatSessionUserMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final ChannelContextUtils channelContextUtils;
    private final MessageHandler messageHandler;


    /**
     * 生成验证码
     * @return
     */
    @Override
    public Result<HashMap<String, String>> checkCode() {
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(100,42);
        String code = captcha.text();
        String checkCodeKey = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(CHECK_CODE_KEY + checkCodeKey, code, CHECK_CODE_TTL, TimeUnit.SECONDS);
        log.info("验证码是{}", code);
        String checkCodeBase64 = captcha.toBase64();
        HashMap<String, String> result = new HashMap<>();
        result.put("checkCode", checkCodeBase64);
        result.put("checkCodeKey", checkCodeKey);
        return Result.success(result);
    }

    /**
     * 注册
     * @param registerDTO
     */
    @Override
    @Transactional
    public Result register(RegisterDTO registerDTO) {

        //检查验证码
        if (!registerDTO.getCheckCode().equalsIgnoreCase((String) redisTemplate.opsForValue().getAndDelete(CHECK_CODE_KEY + registerDTO.getCheckCodeKey()))){
            throw new RuntimeException("验证码错误");
        }
        //判断邮箱账号是否已经存在
        UserInfo userInfo = lambdaQuery().eq(UserInfo::getEmail, registerDTO.getEmail()).one();
        if (userInfo != null){
            throw new BizIllegalException("邮箱账号已存在");
        }

        //创建用户账号
        String userId = UserContactType.USER.getPrefix() + RandomUtil.randomNumbers(11);
        //用户信息打包
        UserInfo user = new UserInfo();
        user.setUserId(userId);
        user.setNickName(registerDTO.getNickName());
        user.setEmail(registerDTO.getEmail());
        //密码加密
        String password = null;
        try {
            password = Md5Utils.encodeMd5(registerDTO.getPassword());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        user.setPassword(password);
        user.setCreateTime(LocalDateTime.now());
        user.setStatus(UserStatus.NORMAL);
        user.setLastOffTime(LocalDateTime.now());
        user.setJoinType(JoinType.CHECK);
        //保存用户信息
        userInfoMapper.insert(user);

        //创建机器人好友
        SysSettingDTO sysSettingDTO = redisComponent.getSysSetting();
        String robotUid = sysSettingDTO.getRobotUid();
        String robotNickName = sysSettingDTO.getRobotNickName();
        String robotWelcome = sysSettingDTO.getRobotWelcome();
        //增加机器人好友
        UserContact userContact = UserContact.builder()
                .userId(userId)
                .contactId(robotUid)
                .contactType(UserContactType.USER)
                .createTime(LocalDateTime.now())
                .lastUpdateTime(LocalDateTime.now())
                .status(UserContactStatus.FRIEND)
                .build();
        userContactMapper.insert(userContact);
        //增加会话信息
        String sessionId = null;
        sessionId = ChatMessageUtils.getChatSessionId4User(new String[]{userId, robotUid});

        ChatSession chatSession = ChatSession.builder()
                .lastMessage(robotWelcome)
                .sessionId(sessionId)
                .lastReceiveTime(LocalDateTime.now())
                .build();
        chatSessionMapper.insert(chatSession);
        //增加会话人信息
        ChatSessionUser chatSessionUser = ChatSessionUser.builder()
                .userId(userId)
                .contactId(robotUid)
                .contactName(robotNickName)
                .sessionId(sessionId)
                .build();
        chatSessionUserMapper.insert(chatSessionUser);

        //增加聊天信息
        ChatMessage chatMessage = ChatMessage.builder()
                .sessionId(sessionId)
                .messageType(MessageType.CHAT)
                .messageContent(robotWelcome)
                .sendUserId(robotUid)
                .sendUserNickName(robotNickName)
                .sendTime(LocalDateTime.now())
                .contactId(userId)
                .contactType(UserContactType.USER)
                .status(MessageStatus.SENT)
                .build();
        chatMessageMapper.insert(chatMessage);

        return Result.success();
    }

    /**
     * 登录
     * @param loginDTO
     * @return
     */
    @Override
    public Result<UserInfoVO> login(LoginDTO loginDTO) {

        //检查验证码
        if (!loginDTO.getCheckCode().equalsIgnoreCase((String) redisTemplate.opsForValue().getAndDelete(CHECK_CODE_KEY + loginDTO.getCheckCodeKey()))){
            throw new BizIllegalException("验证码错误");
        }
        String email = loginDTO.getEmail();
        //对前端传入密码进行加密
        String password = null;
        try {
            password = Md5Utils.encodeMd5(loginDTO.getPassword());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
//        password = loginDTO.getPassword();//test

        UserInfo userInfo = lambdaQuery().eq(UserInfo::getEmail, email).one();
        if (userInfo == null || !password.equals(userInfo.getPassword())) {
            throw new BadRequestException("账号或密码错误");
        }
        if (userInfo.getStatus().equals(UserStatus.FROZEN)){
            throw new ForbiddenException("账户状态异常");
        }

        //查询我的联系人
        List<UserContact> list = Db.lambdaQuery(UserContact.class)
                .eq(UserContact::getUserId, userInfo.getUserId())
                .eq(UserContact::getStatus, UserContactStatus.FRIEND)
                .list();
        List<String> contactIdList = list.stream().map(UserContact::getContactId).collect(Collectors.toList());
        if (!contactIdList.isEmpty()){
            redisComponent.addUserContactBatch(userInfo.getUserId(), contactIdList);
        }

        TokenUserInfoDTO tokenUserInfoDTO = getTokenUserInfoDTO(userInfo);

        Long lastHeartBeat = redisComponent.getUserHeartBeat(userInfo.getUserId());
        if (lastHeartBeat != null){
            throw new BizIllegalException("此账号已在别处登录，请退出后重试");
        }

        //保存登录信息到redis
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, userInfo.getUserId());
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);
        tokenUserInfoDTO.setToken(token);
        redisComponent.saveTokenUserInfoDTO(tokenUserInfoDTO);
        UserInfoVO userInfoVO = new UserInfoVO();
        BeanUtils.copyProperties(userInfo, userInfoVO);
        userInfoVO.setToken(tokenUserInfoDTO.getToken());
        userInfoVO.setAdmin(tokenUserInfoDTO.getAdmin());

        return Result.success(userInfoVO);
    }


    private TokenUserInfoDTO getTokenUserInfoDTO(UserInfo userInfo){
        TokenUserInfoDTO tokenUserInfoDTO = TokenUserInfoDTO.builder()
                .userId(userInfo.getUserId())
                .nickName(userInfo.getNickName())
                .build();

        String adminEmails = appConfig.getAdminEmails();
        if (!StrUtil.isEmpty(adminEmails) && ArrayUtil.contains(adminEmails.replace(" ", "").split(","), userInfo.getEmail())){
            tokenUserInfoDTO.setAdmin(true);
        }else {
            tokenUserInfoDTO.setAdmin(false);
        }

        return tokenUserInfoDTO;
    }

    /**
     * 获取用户信息
     * @param request
     * @return
     */
    @Override
    public Result<UserInfoVO> getUserInfo(HttpServletRequest request) {
        TokenUserInfoDTO tokenUserInfoDTO = redisComponent.getTokenUserInfoDTO(request);
        UserInfo userInfo = userInfoMapper.selectById(tokenUserInfoDTO.getUserId());
        UserInfoVO userInfoVO = BeanUtil.copyProperties(userInfo, UserInfoVO.class);
        userInfoVO.setAdmin(tokenUserInfoDTO.getAdmin());
        return Result.success(userInfoVO);
    }

    /**
     * 更新用户信息
     * @param request
     * @param userInfoDTO
     * @return
     */
    @Override
    @Transactional
    public Result updateUserInfo(HttpServletRequest request, UserInfoDTO userInfoDTO) throws IOException {
        TokenUserInfoDTO tokenUserInfoDTO = redisComponent.getTokenUserInfoDTO(request);
        String userId = tokenUserInfoDTO.getUserId();
        //修改头像
        if (userInfoDTO.getAvatarFile() != null){
            String baseFolder = appConfig.getProjectFolder() + FileConstants.FILE_FOLDER;
            File targetFileFolder = new File(baseFolder + FileConstants.AVATAR_FOLDER);
            if (!targetFileFolder.exists()){
                targetFileFolder.mkdirs();
            }
            String filePath = targetFileFolder.getPath() + "/" + userId + FileConstants.IMAGE_SUFFIX;
            userInfoDTO.getAvatarFile().transferTo(new File(filePath));
            userInfoDTO.getAvatarCover().transferTo(new File(filePath + FileConstants.COVER_IMAGE_SUFFIX));
        }

        //更新相关信息
        userInfoMapper.updateWithUserInfoDTO(userInfoDTO, userId);

        if (userInfoDTO.getNickName() == null){
            return Result.success();
        }
        //更新会话信息中的昵称信息
        Db.lambdaUpdate(ChatSessionUser.class)
                .eq(ChatSessionUser::getContactId, tokenUserInfoDTO.getUserId())
                .set(ChatSessionUser::getContactName, userInfoDTO.getNickName())
                .update();
        //更新缓存中的昵称信息
        tokenUserInfoDTO.setNickName(userInfoDTO.getNickName());
        redisComponent.saveTokenUserInfoDTO(tokenUserInfoDTO);

        return Result.success();
    }

    /**
     * 修改密码
     * @param request
     * @param password
     * @return
     */
    @Override
    public Result updatePassword(HttpServletRequest request, String password) {
        TokenUserInfoDTO tokenUserInfoDTO = redisComponent.getTokenUserInfoDTO(request);
        try {
            password = Md5Utils.encodeMd5(password);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        lambdaUpdate().eq(UserInfo::getUserId, tokenUserInfoDTO.getUserId())
                .set(UserInfo::getPassword, password)
                .update();

        //强制退出，重新登录
        channelContextUtils.closeContact(tokenUserInfoDTO.getUserId());

        return Result.success();
    }

    /**
     * 获取用户列表
     * @param query
     * @return
     */
    @Override
    public Result<PageDTO<UserInfoVO>> loadUser(PageQuery query) {

        Page<UserInfo> page = query.toMpPage("create_time", false);
        userInfoMapper.selectPage(page, null);
        PageDTO<UserInfoVO> of = PageDTO.of(page, UserInfoVO.class);

        return Result.success(of);
    }

    /**
     * 更新用户状态
     * @param status
     * @param userId
     * @return
     */
    @Override
    public Result updateUserStatus(Integer status, String userId) {
        UserStatus userStatus = UserStatus.getByNum(status);
        lambdaUpdate().eq(UserInfo::getUserId, userId)
                .set(UserInfo::getStatus, userStatus)
                .update();
        return Result.success();
    }

    /**
     * 强制下线
     * @param userId
     * @return
     */
    @Override
    public Result forceOffLine(String userId) {
        MessageSendDTO messageSendDTO = MessageSendDTO.builder()
                .contactType(UserContactType.USER)
                .messageType(MessageType.FORCE_OFF_LINE)
                .contactId(userId)
                .build();
        messageHandler.sendMessage(messageSendDTO);
        return Result.success();
    }

}

