package com.hanwj.sample.web.model.user.co;

import com.hanwj.sample.web.model.user.enums.TenantStatusEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 租户名称查询条件
 *
 * @author Hanwenjie
 */
@Data
@ApiModel(value = "租户名称查询条件")
public class TenantCo implements Serializable {
    /**
     * 名称
     */
    @ApiModelProperty(value = "租户名称")
    private String name;

    /**
     * 名称
     */
    @ApiModelProperty(value = "密码")
    private String password;

    /**
     * 冻结标记,9:冻结,0:正常
     */
    @ApiModelProperty(value = "冻结标记,9:冻结,0:正常")
    private TenantStatusEnum status;

}
