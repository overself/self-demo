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
public class DictItemUpdateDto extends BaseData {
    /**
     * 目常量项ID
     */
    @NotBlank(message = "常量项目ID不能为空")
    private String dictItemId;
    /**
     * 类型
     */
    @NotBlank(message = "常量类型不能为空")
    private String type;
    /**
     * 项目编码
     */
    @NotBlank(message = "常量项目值不能为空")
    @Length(max = 20, message = "常量项目输入超长(最大20文字)")
    private String code;
    /**
     * 项目标签
     */
    @NotBlank(message = "常量项目标签不能为空")
    @Length(max = 40, message = "常量项目标签输入超长(最大40文字)")
    private String label;
    /**
     * 是否禁用 (0:有效;1:无效;)
     */
    private Boolean isEnabled = true;
    /**
     * 描述
     */
    @Length(max = 100, message = "描述输入超长(最大100文字)")
    private String description;
    /**
     * 表示顺序
     */
    private Integer indexSeq;

}
