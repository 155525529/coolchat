package com.coolchat.mapper;

import com.coolchat.domain.dto.UserInfoDTO;
import com.coolchat.domain.po.UserInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author yayibai
 * @since 2024-08-13
 */
@Mapper
public interface UserInfoMapper extends BaseMapper<UserInfo> {

    void updateWithUserInfoDTO(@Param("userInfo") UserInfoDTO userInfoDTO, @Param("userId") String userId);
}
