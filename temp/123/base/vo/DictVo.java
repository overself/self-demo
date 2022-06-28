package com.jjl.dxz.module.biz.workflow.base.vo;

import com.jjl.features.common.model.vo.BaseVo;
import lombok.Data;

/**
 * 数据字典分类 实体
 *
 * @author HanWenjie
 */
@Data
public class DictVo extends BaseVo {
    /**
     * 数据ID
     */
    private String dictId;
    /**
     * 类型	type
     */
    private String type;
    /**
     * 是否禁用 (0:有效;1:无效;)
     */
    private Boolean isInvalid;
    /**
     * 是否是系统内置 (0:否; 1:是;)
     */
    private Boolean isSystem = false;
    /**
     * 描述
     */
    private String description;
    /**
     * 依赖数据定义
     */
    private String dependencyType;
    /**
     * 依赖常量描述
     */
    private String dependencyDesc;
    /**
     * 备注
     */
    private String remarks;
}
