package com.coolchat.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.time.LocalDateTime;
import java.io.Serializable;

import com.coolchat.enums.FileType;
import com.coolchat.enums.PublishStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author yayibai
 * @since 2024-11-30
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("app_update")
@ApiModel(value="AppUpdate对象", description="")
public class AppUpdate implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "自增id")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "版本号")
    private String version;

    @ApiModelProperty(value = "更新描述")
    private String updateDesc;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "状态 0:未发布 1:灰度发布 2:全网发布")
    private PublishStatus status;

    @ApiModelProperty(value = "灰度uid")
    private String grayscaleUid;

    @ApiModelProperty(value = "文件类型 0:本地文件 1:外链")
    private FileType fileType;

    @ApiModelProperty(value = "外链地址")
    private String outerLink;


}
