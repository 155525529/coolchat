package com.coolchat.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coolchat.domain.po.UserContactApply;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coolchat.domain.vo.ApplySearchVO;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author yayibai
 * @since 2024-08-20
 */
public interface UserContactApplyMapper extends BaseMapper<UserContactApply> {

    @Select("SELECT a.*, CASE WHEN a.contact_type = 0 THEN u.nick_name WHEN a.contact_type = 1 THEN g.group_name END AS contactName FROM user_contact_apply a LEFT JOIN user_info u ON u.user_id = a.apply_user_id LEFT JOIN group_info g ON g.group_id = a.contact_id WHERE receive_user_id = #{userId}")
    Page<ApplySearchVO> getApplyInfo(Page<ApplySearchVO> page, String userId);
}
