package com.jjl.dxz.module.biz.workflow.base.dto;

import com.jjl.features.common.dto.BaseData;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

/**
 * 数据字典分类 实体
 *
 * @author HanWenjie
 */
@Data
public class DictCreateDto extends BaseData {
    /**
     * 类型	type
     */
    @NotBlank(message = "数据类型不能为空")
    @Length(max = 20, message = "数据类型输入超长(最大20文字)")
    private String type;
    /**
     * 是否禁用	is_enabled (0:有效;1:无效;)
     */
    private Boolean isEnabled = false;
    /**
     * 描述	description
     */
    @Length(max = 100, message = "描述输入超长(最大100文字)")
    private String description;
    /**
     * 依赖数据定义	dependency_type
     */
    private String dependencyType;
    /**
     * 备注
     */
    @Length(max = 250, message = "备注输入超长(最大250文字)")
    private String remarks;
    /**
     * 是否是系统内置 (0:否; 1:是;)
     */
    private Boolean isSystem = false;

}
