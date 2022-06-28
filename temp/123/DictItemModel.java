package com.jjl.dxz.module.biz.workflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.jjl.features.common.dto.BaseData;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据项目定义 实体
 *
 * @author HanWenjie
 */
@Data
@TableName("dxz_dict_item")
public class DictItemModel extends BaseData {
    /**
     * 数据ID	dict_item_id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String dictItemId;
    /**
     * 类型字典项	type
     */
    private String type;
    /**
     * 类型字典项编码
     */
    private String code;
    /**
     * 类型字典项值
     */
    private String label;
    /**
     * 描述	description
     */
    private String description;
    /**
     * 表示顺序	index_seq
     */
    private Integer indexSeq;
    /**
     * 是否禁用	is_invalid (0:有效;1:无效;)
     */
    private Boolean isInvalid;
    /**
     * 租户编码	tenant_code
     */
    private String tenantCode;
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

}
