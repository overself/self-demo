package com.jjl.dxz.module.biz.workflow.base.dto;

import com.jjl.features.common.dto.BaseData;
import lombok.Data;

/**
 * 数据字典分类 实体
 *
 * @author HanWenjie
 */
@Data
public class DictQueryDto extends BaseData {
    /**
     * 类型
     */
    private String type;
    /**
     * 是否禁用 (0:有效;1:无效;)
     */
    private Boolean isInvalid;
    /**
     * 是否系统字典 (0:不是;1:是;)
     */
    private Boolean isSystem;
    /**
     * 描述
     */
    private String description;

}
