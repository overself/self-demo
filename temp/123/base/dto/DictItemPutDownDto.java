package com.jjl.dxz.module.biz.workflow.base.dto;

import com.jjl.features.common.dto.BaseData;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 数据字典分类 实体
 *
 * @author HanWenjie
 */
@Data
public class DictItemPutDownDto extends BaseData {
    /**
     * 数据类型
     */
    private String type;
    /**
     * 数据项key
     */
    private String code;
    /**
     * 限制获取范围
     */
    private List<String> rangeCodes;

    /**
     * 过滤掉不需要的
     */
    private List<String> excludeCodes;

}
