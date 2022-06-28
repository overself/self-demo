package com.jjl.dxz.module.biz.workflow.base.dto;

import com.jjl.features.common.dto.BaseData;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 数据字典分类 实体
 *
 * @author HanWenjie
 */
@Data
public class DictItemQueryDto extends BaseData {
    /**
     * 类型
     */
    @NotBlank(message = "常量类型不能为空")
    private String type;
    /**
     * 是否禁用	(0:有效;1:无效;)
     */
    private Boolean isEnabled;
    /**
     * 描述
     */
    private String description;

}
