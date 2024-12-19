package com.coolchat.domain.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import com.coolchat.enums.JoinType;
import com.coolchat.enums.UserStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author yayibai
 * @since 2024-08-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user_info")
@ApiModel(value="UserInfo对象", description="")
public class UserInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "用户id")
    @TableId(value = "user_id", type = IdType.INPUT)
    private String userId;

    @ApiModelProperty(value = "邮箱")
    @TableField("email")
    private String email;

    @ApiModelProperty(value = "昵称")
    @TableField("nick_name")
    private String nickName;

    @ApiModelProperty(value = "0:直接加好友  1:同意后加好友")
    @TableField("join_type")
    private JoinType joinType;

    @ApiModelProperty(value = "性别 0:女 1:男")
    @TableField("sex")
    private Integer sex;

    @ApiModelProperty(value = "密码")
    @TableField("password")
    private String password;

    @ApiModelProperty(value = "个性签名")
    @TableField("personal_signature")
    private String personalSignature;

    @ApiModelProperty(value = "状态")
    @TableField("status")
    private UserStatus status;

    @ApiModelProperty(value = "创建时间")
    @TableField("create_time")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "最后上线时间")
    @TableField("last_login_time")
    private LocalDateTime lastLoginTime;

    @ApiModelProperty(value = "最后离线时间")
    @TableField("last_off_time")
    private LocalDateTime lastOffTime;

    @ApiModelProperty(value = "地区")
    @TableField("area_name")
    private String areaName;

    @ApiModelProperty(value = "地区编号")
    @TableField("area_code")
    private String areaCode;


}
