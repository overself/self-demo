package com.jjl.dxz.module.biz.workflow.base.dto;

import com.jjl.features.common.dto.BaseData;
import lombok.Data;

import java.util.List;

/**
 * 数据字典分类 实体
 *
 * @author HanWenjie
 */
@Data
public class DictItemPutDownBatchDto extends BaseData {
    /**
     * 简单查询
     */
    private List<String> typeCodes;
    /**
     * 复杂查询查询
     */
    private List<DictItemPutDownDto> specialTypeCodes;

}
