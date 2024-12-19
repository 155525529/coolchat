package com.coolchat.util;

import cn.hutool.core.util.StrUtil;
import com.coolchat.constants.RedisConstants;
import com.coolchat.domain.dto.SysSettingDTO;
import com.coolchat.domain.dto.TokenUserInfoDTO;
import com.coolchat.exception.BizIllegalException;
import io.netty.channel.Channel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Component
public class RedisComponent {

    @Autowired
    private RedisTemplate redisTemplate;

    public Long getUserHeartBeat(String userId){
        return (Long) redisTemplate.opsForValue().get(RedisConstants.WS_USER_HEART_BEAT_KEY + userId);
    }

    public void saveHeartBeat(String userId){
        redisTemplate.opsForValue().set(RedisConstants.WS_USER_HEART_BEAT_KEY + userId, System.currentTimeMillis(), RedisConstants.WS_USER_HEART_BEAT_TTL, TimeUnit.SECONDS);
    }

    public void removeHeartBeat(String userId) {
        redisTemplate.delete(RedisConstants.WS_USER_HEART_BEAT_KEY + userId);
    }


    public void saveTokenUserInfoDTO(TokenUserInfoDTO tokenUserInfoDTO){
        redisTemplate.opsForValue().set(RedisConstants.WS_TOKEN_KEY + tokenUserInfoDTO.getToken(), tokenUserInfoDTO, RedisConstants.WS_TOKEN_TTL, TimeUnit.DAYS);
        redisTemplate.opsForValue().set(RedisConstants.WS_TOKEN_USERID_KEY + tokenUserInfoDTO.getUserId(), tokenUserInfoDTO.getToken(), RedisConstants.WS_TOKEN_TTL, TimeUnit.DAYS);

    }

    public void saveSysSetting(SysSettingDTO sysSettingDTO){
    redisTemplate.opsForValue().set(RedisConstants.SYS_SETTING_KEY, sysSettingDTO);
    }

    public SysSettingDTO getSysSetting(){
        SysSettingDTO sysSettingDTO = (SysSettingDTO) redisTemplate.opsForValue().get(RedisConstants.SYS_SETTING_KEY);
        sysSettingDTO = sysSettingDTO == null ? new SysSettingDTO() : sysSettingDTO;
        return sysSettingDTO;
    }

    public TokenUserInfoDTO getTokenUserInfoDTO(HttpServletRequest request){
        String token = request.getHeader("token");
        TokenUserInfoDTO tokenUserInfoDTO = (TokenUserInfoDTO) redisTemplate.opsForValue().get(RedisConstants.WS_TOKEN_KEY + token);
        if (tokenUserInfoDTO == null){
            throw new BizIllegalException("未知账户");
        }
        return tokenUserInfoDTO;
    }

    public TokenUserInfoDTO getTokenUserInfoDTO(String token){
        TokenUserInfoDTO tokenUserInfoDTO = (TokenUserInfoDTO) redisTemplate.opsForValue().get(RedisConstants.WS_TOKEN_KEY + token);
        return tokenUserInfoDTO;
    }

    public void cleanUserTokenByUserId(String userId){
        String token = (String) redisTemplate.opsForValue().get(RedisConstants.WS_TOKEN_USERID_KEY + userId);
        if (StrUtil.isEmpty(token)){
            return;
        }
        redisTemplate.delete(RedisConstants.WS_TOKEN_KEY + token);
    }

    public void saveChannel(String userId, Channel channel){
        redisTemplate.opsForValue().set(RedisConstants.WS_TOKEN_KEY + userId, channel, RedisConstants.WS_TOKEN_TTL, TimeUnit.DAYS);
    }

    //批量添加联系人
    public void addUserContactBatch(String userId, List<String> contactIdList){
        redisTemplate.opsForValue().set(RedisConstants.USER_CONTACT_KEY + userId, contactIdList, RedisConstants.WS_TOKEN_TTL, TimeUnit.DAYS);
    }

    //添加联系人
    public void addUserContact(String userId, String contactId){
        List<String> contactIdList = getUserContactIdList(userId);
        if (contactIdList.contains(contactId)){
            return;
        }
        contactIdList.add(contactId);
        redisTemplate.opsForValue().set(RedisConstants.USER_CONTACT_KEY + userId, contactIdList, RedisConstants.WS_TOKEN_TTL, TimeUnit.DAYS);
    }

    public List<String> getUserContactIdList(String userId){
        return (List<String>) redisTemplate.opsForValue().get(RedisConstants.USER_CONTACT_KEY + userId);
    }

    public void removeUserContact(String userId, String contactId){
        List<String> contactIdList = getUserContactIdList(userId);
        if (contactIdList == null){
            return;
        }
        if (!contactIdList.contains(contactId)){
            return;
        }
        contactIdList.remove(contactId);
        redisTemplate.opsForValue().set(RedisConstants.USER_CONTACT_KEY + userId, contactIdList, RedisConstants.WS_TOKEN_TTL, TimeUnit.DAYS);

    }
}
