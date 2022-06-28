package com.jjl.dxz.module.biz.workflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.jjl.features.common.dto.BaseData;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据字典分类 实体
 *
 * @author HanWenjie
 */
@Data
@TableName("dxz_dict")
public class DictModel extends BaseData {
    /**
     * 数据ID dict_id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String dictId;
    /**
     * 类型	type
     */
    private String type;
    /**
     * 是否禁用	is_invalid (0:有效;1:无效;)
     */
    private Boolean isInvalid;
    /**
     * 是否是系统内置 is_system (0:否; 1:是;)
     */
    private Boolean isSystem = false;
    /**
     * 描述	description
     */
    private String description;
    /**
     * 租户编码	tenant_code
     */
    private String tenantCode;
    /**
     * 依赖数据定义	dependency_type
     */
    private String dependencyType;
    /**
     * 备注
     */
    private String remarks;
    /**
     * 数据删除标识 (0:有效;1:删除;)
     */
    @TableLogic
    private Boolean isDeleted;
    /**
     * created_at
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    /**
     * updated_at
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    public boolean isSystem() {
        if (this.getIsSystem() != null) {
            return this.getIsSystem();
        } else {
            return false;
        }
    }
}
