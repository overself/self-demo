package com.hanwj.sample.web.model.user.vo;

import com.hanwj.sample.web.model.user.enums.TenantStatusEnum;
import com.hanwj.web.model.vo.BaseVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDate;

/**
 * 租户
 *
 * @author developer
 */
@Data
@ApiModel(value = "租户信息")
public class TenantVo extends BaseVo {

    private static final long serialVersionUID = 1L;

    /**
     * 手机号
     */
    @ApiModelProperty(value = "手机号")
    private String phone;

    /**
     * 数据ID
     */
    @ApiModelProperty(value = "数据ID")
    private String id;
    /**
     * 租户名称
     */
    @ApiModelProperty(value = "租户名称")
    private String name;
    /**
     * 租户编号
     */
    @ApiModelProperty(value = "租户编号")
    private String code;
    /**
     * 密码
     */
    @ApiModelProperty(value = "密码")
    private String password;
    /**
     * 开始时间
     */
    @ApiModelProperty(value = "开始时间")
    private LocalDate startTime;
    /**
     * 结束时间
     */
    @ApiModelProperty(value = "结束时间")
    private LocalDate endTime;
    /**
     * 租户冻结标记,9:冻结,0:正常
     */
    @ApiModelProperty(value = "租户冻结标记,9:冻结,0:正常")
    private TenantStatusEnum status;
}
