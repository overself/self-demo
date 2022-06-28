package com.jjl.dxz.module.biz.workflow.base.service;

import com.jjl.dxz.module.biz.workflow.base.dto.*;
import com.jjl.dxz.module.biz.workflow.base.vo.DictItemPutDownVo;
import com.jjl.dxz.module.biz.workflow.base.vo.DictItemVo;
import com.jjl.features.web.model.PageCondition;
import com.jjl.features.web.model.ResultResponse;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 常量项目 服务接口
 *
 * @author HanWenjie
 */
public interface DictItemService {

    /**
     * 分页查询常量项目列表
     *
     * @param condition 查询条件
     * @return 常量项目列表
     */
    ResultResponse<List<DictItemVo>> selectDictItemPage(PageCondition<DictItemQueryDto> condition);

    /**
     * 查询常量项目下拉列表
     *
     * @param putDownDto 查询条件
     * @return 常量项目下拉列表
     */
    ResultResponse<List<DictItemPutDownVo>> selectDictPutDown(DictItemPutDownDto putDownDto);

    /**
     * 查询常量项目下拉选项(可简单批量，可综合批量)
     *
     * @param batchDto 常量项目下拉选项条件
     *                 typeCodes：但指定字典类型，全量查询  List<String>
     *                 typeList：可综合查询字典类型  List<DictItemPutDownDto>
     * @return 常量项目下拉选项列表<Key:字典类型, value:字典列表 < List < DictItemPutDownVo>>
     */
    ResultResponse<Map<String, List<DictItemPutDownVo>>> selectDictPutDownBatch(DictItemPutDownBatchDto putDownBatchDto);

    /**
     * 创建常量项目
     *
     * @param createDto 常量项目
     * @return 是否成功
     */
    boolean saveDictItem(DictItemCreateDto createDto);

    /**
     * 更新常量项目
     *
     * @param updateDto 常量项目
     * @return 是否成功
     */
    boolean updateDictItem(DictItemUpdateDto updateDto);

    /**
     * 设置常量项目的有效性
     *
     * @param id
     * @param isEnabled
     * @return
     */
    boolean enabledDictItem(Serializable id, boolean isEnabled);

    /**
     * 删除常量项目
     *
     * @param id 常量项目Id
     * @return 是否成功
     */
    boolean delDictItem(Serializable id);

    /**
     * 根据常量类型，批量删除常量项目
     *
     * @param type 常量类型
     * @return 是否成功
     */
    boolean delDictItemByType(String type);

    /**
     * 更新常量项目的常量类型
     *
     * @param fromDictType 变更前常量类型
     * @param toDictType   变更后常量类型
     * @return 是否有更新
     */
    boolean updateDictTypeByType(String fromDictType, String toDictType);

    /**
     * 根据常量类型，获得该类型下的label列表
     *
     * @param dictType 数据项类型
     * @return 常量类型的label列表
     */
    Map<String, List<String>> getDictLabels(@NotNull List<String> dictType);

    /**
     * 获取type下所有值
     *
     * @param type type
     * @return List<DictItemVo>
     */
    List<DictItemVo> loadByType(String type);

}
