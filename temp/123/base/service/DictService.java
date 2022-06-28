package com.jjl.dxz.module.biz.workflow.base.service;

import com.jjl.dxz.module.biz.workflow.base.dto.DictCreateDto;
import com.jjl.dxz.module.biz.workflow.base.dto.DictQueryDto;
import com.jjl.dxz.module.biz.workflow.base.dto.DictUpdateDto;
import com.jjl.dxz.module.biz.workflow.base.vo.DictVo;
import com.jjl.features.web.model.PageCondition;
import com.jjl.features.web.model.ResultResponse;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 数据字典分类 服务接口
 *
 * @author HanWenjie
 */
public interface DictService {

    /**
     * 分页查询数据字典项目列表
     *
     * @param condition 查询条件
     * @return 数据字典项列表
     */
    ResultResponse<List<DictVo>> selectDictPage(PageCondition<DictQueryDto> condition);

    /**
     * 创建常量
     *
     * @param createDto 数据项信息
     * @return 创建是否成功
     */
    boolean saveDict(DictCreateDto createDto);

    /**
     * 创建常量
     *
     * @param updateDto 数据项信息
     * @return 创建是否成功
     */
    boolean updateDict(DictUpdateDto updateDto);

    /**
     * 设置数据项是否有效的状态
     *
     * @param id        数据项ID
     * @param isEnabled 是否有效
     * @return 是否成功
     */
    boolean enabledDict(Serializable id, boolean isEnabled);

    /**
     * 删除指定的数据项目类型
     *
     * @param id 数据项ID
     * @return 是否成功
     */
    boolean delDict(Serializable id);

    /**
     * 根据类型判断数据项是否存在
     *
     * @param dictType 数据项类型
     * @param dictId   常量ID
     * @return 是否存在
     */
    boolean existDictType(@NotNull String dictType, String dictId);

    /**
     * 根据类型判断数据项是系统类型
     *
     * @param dictType 数据项类型
     * @return 是否存在
     */
    boolean isSystemDictType(@NotNull String dictType);

    /**
     * 根据指定的类型参数，筛除无效的常量定义
     * @param dictType
     * @return
     */
    List<String> filterOutInvalidType(@NotNull List<String> dictType);

}
