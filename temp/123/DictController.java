package com.jjl.dxz.module.biz.workflow.console.controller;

import com.jjl.dxz.module.biz.workflow.base.dto.*;
import com.jjl.dxz.module.biz.workflow.base.service.DictItemService;
import com.jjl.dxz.module.biz.workflow.base.service.DictService;
import com.jjl.dxz.module.biz.workflow.base.vo.DictItemPutDownVo;
import com.jjl.dxz.module.biz.workflow.base.vo.DictItemVo;
import com.jjl.dxz.module.biz.workflow.base.vo.DictVo;
import com.jjl.features.web.model.PageCondition;
import com.jjl.features.web.model.ResultResponse;
import com.jjl.features.web.util.ResultUtil;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 常量定义及常量项目 CTL
 *
 * @author HanWenjie
 */
@RestController
@RequestMapping("/api/wf/c/dict")
public class DictController {

    @DubboReference
    private DictService dictService;

    @DubboReference
    private DictItemService dictItemService;

    /**
     * 查询常量列表
     *
     * @param condition 常量查询条件
     * @return 常量列表
     */
    @PostMapping("/type/page")
    public ResultResponse<List<DictVo>> getDictTypePage(@RequestBody @Validated PageCondition<DictQueryDto> condition) {
        return dictService.selectDictPage(condition);
    }

    /**
     * 创建常量
     *
     * @param createDto 常量
     * @return 操作结果<是否成功>
     */
    @PostMapping("/type/save")
    public ResultResponse<Boolean> saveDictType(@RequestBody @Validated DictCreateDto createDto) {
        return ResultUtil.ok().putData(dictService.saveDict(createDto));
    }

    /**
     * 更新常量
     *
     * @param updateDto 常量
     * @return 操作结果<是否成功>
     */
    @PostMapping("/type/upd")
    public ResultResponse<Boolean> updateDictType(@RequestBody @Validated DictUpdateDto updateDto) {
        return ResultUtil.ok().putData(dictService.updateDict(updateDto));
    }

    /**
     * 删除常量
     *
     * @param dictId 常量ID
     * @return 操作结果<是否成功>
     */
    @DeleteMapping("/type/del/{dictId}")
    public ResultResponse<Boolean> deleteDictType(@PathVariable("dictId") String dictId) {
        return ResultUtil.ok().putData(dictService.delDict(dictId));
    }

    /**
     * 设置常量的有效性
     *
     * @param dictId    常量ID
     * @param isInvalid 是否有效
     * @return 操作结果<是否成功>
     */
    @PostMapping("/type/enabled/{dictId}")
    public ResultResponse<Boolean> enabledDictType(@PathVariable("dictId") String dictId, @RequestParam("invalid") Boolean isInvalid) {
        if (isInvalid == null) {
            isInvalid = Boolean.FALSE;
        }
        return ResultUtil.ok().putData(dictService.enabledDict(dictId, isInvalid));
    }

    /**
     * 查询常量项目列表
     *
     * @param condition 常量项目查询条件
     * @return 常量项目列表
     */
    @PostMapping("/item/page")
    public ResultResponse<List<DictItemVo>> getDictItemPage(@RequestBody @Validated PageCondition<DictItemQueryDto> condition) {
        return dictItemService.selectDictItemPage(condition);
    }

    /**
     * 查询常量项目下拉选项
     *
     * @param putDownDto 常量项目下拉选项条件
     * @return 常量项目下拉选项列表
     */
    @PostMapping("/put-down")
    public ResultResponse<List<DictItemPutDownVo>> getDictItemPage(@RequestBody @Validated DictItemPutDownDto putDownDto) {
        return dictItemService.selectDictPutDown(putDownDto);
    }

    /**
     * 查询常量项目下拉选项(可简单批量，可综合批量)
     *
     * @param batchDto 常量项目下拉选项条件
     *                 typeCodes：但指定字典类型，全量查询  List<String>
     *                 typeList：可综合查询字典类型  List<DictItemPutDownDto>
     * @return 常量项目下拉选项列表
     */
    @PostMapping("/batch/put-down")
    public ResultResponse<Map<String, List<DictItemPutDownVo>>> getDictItemPage(@RequestBody @Validated DictItemPutDownBatchDto batchDto) {
        return dictItemService.selectDictPutDownBatch(batchDto);
    }

    /**
     * 创建常量项目
     *
     * @param createDto 常量项目
     * @return 操作结果<是否成功>
     */
    @PostMapping("/item/save")
    public ResultResponse<Boolean> saveDictItem(@RequestBody @Validated DictItemCreateDto createDto) {
        return ResultUtil.ok().putData(dictItemService.saveDictItem(createDto));
    }

    /**
     * 更新常量项目
     *
     * @param updateDto 常量项目
     * @return 操作结果<是否成功>
     */
    @PostMapping("/item/upd")
    public ResultResponse<Boolean> updateDictItem(@RequestBody @Validated DictItemUpdateDto updateDto) {
        return ResultUtil.ok().putData(dictItemService.updateDictItem(updateDto));
    }

    /**
     * 删除常量项目
     *
     * @param dictItemId 常量项目ID
     * @return 操作结果<是否成功>
     */
    @DeleteMapping("/item/del/{dictItemId}")
    public ResultResponse<Boolean> deleteDictItem(@PathVariable("dictItemId") String dictItemId) {
        return ResultUtil.ok().putData(dictItemService.delDictItem(dictItemId));
    }

    /**
     * 设置常量项目的有效性
     *
     * @param dictItemId 常量项目ID
     * @param isInvalid  是否有效
     * @return 操作结果<是否成功>
     */
    @PostMapping("/item/enabled/{dictItemId}")
    public ResultResponse<Boolean> enabledDictItem(@PathVariable("dictItemId") String dictItemId, @RequestParam("invalid") Boolean isInvalid) {
        if (isInvalid == null) {
            isInvalid = Boolean.FALSE;
        }
        return ResultUtil.ok().putData(dictItemService.enabledDictItem(dictItemId, isInvalid));
    }

}
