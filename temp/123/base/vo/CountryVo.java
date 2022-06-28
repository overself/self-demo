package com.jjl.dxz.module.biz.workflow.base.vo;

import com.jjl.features.common.model.vo.BaseVo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据字典分类 实体
 *
 * @author HanWenjie
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CountryVo extends BaseVo {
    /**
     * 编码
     */
    private String code;
    /**
     * 名称
     */
    private String name;

}
