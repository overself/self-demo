package com.jjl.dxz.module.biz.workflow.base.vo;

import com.jjl.features.common.model.vo.BaseVo;
import lombok.Data;

/**
 * 常量定义下拉
 *
 * @author HanWenjie
 */
@Data
public class DictItemPutDownVo extends BaseVo {
    /**
     * 类型
     */
    private String type;
    /**
     * 编码
     */
    private String code;
    /**
     * 标签
     */
    private String label;

}
