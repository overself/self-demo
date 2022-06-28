package com.jjl.dxz.module.biz.workflow.base.dto;

import com.jjl.features.common.dto.BaseData;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

/**
 * 常量项目 实体
 *
 * @author HanWenjie
 */
@Data
public class DictItemCreateDto extends BaseData {
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
    @Length(max = 60, message = "常量项目标签输入超长(最大60文字)")
    private String label;
    /**
     * 是否禁用 (0:有效;1:无效;)
     */
    private Boolean isEnabled = true;
    /**
     * 描述
     */
    @Length(max = 250, message = "描述输入超长(最大250文字)")
    private String description;
    /**
     * 表示顺序
     */
    private Integer indexSeq;
}
