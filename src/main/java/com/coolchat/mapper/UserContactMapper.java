package com.coolchat.mapper;

import com.coolchat.domain.po.UserContact;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coolchat.domain.vo.UserContactVO;
import com.coolchat.enums.UserContactStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author yayibai
 * @since 2024-08-20
 */
@Mapper
public interface UserContactMapper extends BaseMapper<UserContact> {

    @Select("select * from user_contact where user_id = #{userId} and contact_id = #{contactId}")
    UserContact getByUserIdAndContactId(String userId, String contactId);

    @Select("SELECT c.*, u.nick_name,u.sex FROM user_contact c INNER JOIN user_info u ON u.user_id = c.user_id WHERE contact_id = #{groupId} and c.status = #{userContactStatus}")
    List<UserContact> getListOfContact(String groupId, UserContactStatus userContactStatus);

    @Select("SELECT  c.*, u.nick_name name, u.sex FROM user_contact c INNER JOIN user_info u ON c.contact_id = u.user_id WHERE c.user_id = #{userId} AND c.`status` IN ( 1, 3, 5 ) ORDER BY last_update_time DESC")
    List<UserContactVO> getContactOfFriendList(String userId);

    @Select("SELECT c.*, g.group_name name FROM user_contact c INNER JOIN group_info g ON g.group_id = c.contact_id WHERE c.user_id = #{userId} AND c.`status` IN ( 1, 3, 5 ) ORDER BY last_update_time DESC")
    List<UserContactVO> getContactOfGroupList(String userId);
}
