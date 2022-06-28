package com.jjl.dxz.module.biz.workflow.base.vo;

import com.jjl.features.common.model.vo.BaseVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 国际区号 实体
 *
 * @author liupeng
 */
@Data
@ApiModel
public class CountryCodeVo extends BaseVo {
    /**
     * 国家名称中文
     */
    @ApiModelProperty(value = "国家名称中文")
    private String nameZh;
    /**
     * 国家名称英文
     */
    @ApiModelProperty(value = "国家名称英文")
    private String nameEn;
    /**
     * 国家代码
     */
    @ApiModelProperty(value = "国家代码")
    private String countryCode;
    /**
     * 区域代码
     */
    @ApiModelProperty(value = "区域代码")
    private String areaCode;
    /**
     * 国家描述
     */
    @ApiModelProperty(value = "国家描述")
    private String description;
    /**
     * 电话区号
     */
    @ApiModelProperty(value = "电话区号")
    private String telCode;

}
