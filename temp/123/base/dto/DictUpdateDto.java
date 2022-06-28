package com.jjl.dxz.module.biz.workflow.base.dto;

import com.jjl.features.common.dto.BaseData;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

/**
 * 数据字典常量 实体
 *
 * @author HanWenjie
 */
@Data
public class DictUpdateDto extends BaseData {
    /**
     * 常量Id
     */
    @NotBlank(message = "常量不能为空")
    private String dictId;
    /**
     * 类型
     */
    @NotBlank(message = "常量类型不能为空")
    @Length(max = 20, message = "常量类型输入超长(最大20文字)")
    private String type;
    /**
     * 描述
     */
    @Length(max = 100, message = "描述输入超长(最大100文字)")
    private String description;
    /**
     * 依赖数据定义
     */
    private String dependencyType;
    /**
     * 备注
     */
    @Length(max = 250, message = "备注输入超长(最大250文字)")
    private String remarks;
}
