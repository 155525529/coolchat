package com.coolchat.mapper;

import com.coolchat.domain.po.ChatSessionUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coolchat.domain.vo.ChatSessionUserVO;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author yayibai
 * @since 2024-12-03
 */
public interface ChatSessionUserMapper extends BaseMapper<ChatSessionUser> {

    @Select("SELECT u.*, c.last_message, c.last_receive_time, CASE WHEN SUBSTRING( contact_id, 1, 1 ) = 'G' THEN ( SELECT COUNT( 1 ) FROM user_contact uc WHERE uc.contact_id = u.contact_id ) ELSE 0 END member_count FROM chat_session_user u INNER JOIN chat_session c ON c.session_id = u.session_id WHERE user_id = #{userId} order by last_receive_time desc")
    List<ChatSessionUserVO> getChatSessionInfo(String userId);
}
