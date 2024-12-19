package com.coolchat.mapper;

import com.coolchat.domain.po.AppUpdate;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author yayibai
 * @since 2024-11-30
 */
public interface AppUpdateMapper extends BaseMapper<AppUpdate> {

    @Select("SELECT * FROM app_update  WHERE CAST( SUBSTRING_INDEX( version, '.', 1 ) AS UNSIGNED ) > CAST( SUBSTRING_INDEX( #{version}, '.', 1 ) AS UNSIGNED ) OR (CAST( SUBSTRING_INDEX( version, '.', 1 ) AS UNSIGNED ) = CAST( SUBSTRING_INDEX( #{version}, '.', 1 ) AS UNSIGNED ) AND CAST( SUBSTRING_INDEX( SUBSTRING_INDEX( version, '.', 2 ), '.', - 1 ) AS UNSIGNED ) > CAST( SUBSTRING_INDEX( SUBSTRING_INDEX( #{version}, '.', 2 ), '.', - 1 ) AS UNSIGNED ) ) OR (CAST( SUBSTRING_INDEX( version, '.', 1 ) AS UNSIGNED ) = CAST( SUBSTRING_INDEX( #{version}, '.', 1 ) AS UNSIGNED ) AND CAST( SUBSTRING_INDEX( SUBSTRING_INDEX( version, '.', 2 ), '.', - 1 ) AS UNSIGNED ) = CAST( SUBSTRING_INDEX( SUBSTRING_INDEX( #{version}, '.', 2 ), '.', - 1 ) AS UNSIGNED ) AND CAST( SUBSTRING_INDEX( version, '.', - 1 ) AS UNSIGNED ) > CAST( SUBSTRING_INDEX( #{version}, '.', - 1 ) AS UNSIGNED ) ) AND ( `status` = 2 OR ( `status` = 1 AND FIND_IN_SET( #{uid}, grayscale_uid ) ) ) ORDER BY id DESC LIMIT 1;")
    AppUpdate getLatestUpdate(String version, String uid);
}
