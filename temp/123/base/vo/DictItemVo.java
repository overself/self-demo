package com.jjl.dxz.module.biz.workflow.base.vo;

import com.jjl.features.common.model.vo.BaseVo;
import lombok.Data;

/**
 * 数据字典分类 实体
 *
 * @author HanWenjie
 */
@Data
public class DictItemVo extends BaseVo {
    /**
     * 目常量项ID
     */
    private String dictItemId;
    /**
     * 类型
     */
    private String type;
    /**
     * 项目编码
     */
    private String code;
    /**
     * 项目标签
     */
    private String label;
    /**
     * 是否禁用 (0:有效;1:无效;)
     */
    private Boolean isInvalid;
    /**
     * 描述
     */
    private String description;
    /**
     * 表示顺序
     */
    private Integer indexSeq;

}
