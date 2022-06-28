package com.jjl.dxz.module.biz.workflow.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jjl.dxz.framework.common.util.BeanUtil;
import com.jjl.dxz.framework.common.util.CollUtil;
import com.jjl.dxz.framework.redis.lock.annotation.LockAction;
import com.jjl.dxz.module.biz.workflow.base.dto.*;
import com.jjl.dxz.module.biz.workflow.base.service.DictItemService;
import com.jjl.dxz.module.biz.workflow.base.service.DictService;
import com.jjl.dxz.module.biz.workflow.base.vo.DictItemPutDownVo;
import com.jjl.dxz.module.biz.workflow.base.vo.DictItemVo;
import com.jjl.dxz.module.biz.workflow.entity.DictItemModel;
import com.jjl.dxz.module.biz.workflow.mapper.DictItemMapper;
import com.jjl.features.exception.BusinessException;
import com.jjl.features.web.model.PageCondition;
import com.jjl.features.web.model.Query;
import com.jjl.features.web.model.ResultResponse;
import com.jjl.features.web.util.ResultUtil;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 常量项目服务实现类
 *
 * @author HanWenjie
 */
@DubboService
public class DictItemServiceImpl extends ServiceImpl<DictItemMapper, DictItemModel> implements DictItemService {

    @DubboReference
    private DictService dictService;

    /**
     * @see DictItemService#selectDictItemPage(PageCondition)
     */
    @Override
    public ResultResponse<List<DictItemVo>> selectDictItemPage(PageCondition<DictItemQueryDto> condition) {
        Page<DictItemModel> page = new Query<>(condition);
        DictItemQueryDto con = condition.getCondition();
        if (ObjectUtil.isNull(con)) {
            throw new BusinessException("常量类型不能为空");
        }
        LambdaQueryWrapper<DictItemModel> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(DictItemModel::getType, con.getType());
        wrapper.like(StrUtil.isNotBlank(con.getDescription()), DictItemModel::getDescription, con.getDescription());
        wrapper.eq(ObjectUtil.isNotNull(con.getIsEnabled()), DictItemModel::getIsInvalid, con.getIsEnabled());
        wrapper.orderByAsc(DictItemModel::getIndexSeq);
        IPage<DictItemModel> iPage = baseMapper.selectPage(page, wrapper);
        return ResultUtil.ok().putData(BeanUtil.toBean(iPage.getRecords(), DictItemVo.class)).withPageInfo(condition);
    }

    /**
     * @see DictItemService#selectDictPutDown(DictItemPutDownDto)
     */
    @Override
    public ResultResponse<List<DictItemPutDownVo>> selectDictPutDown(DictItemPutDownDto putDownDto) {
        if (StrUtil.isBlank(putDownDto.getType())) {
            return ResultUtil.ok().putData(new ArrayList<>());
        }
        if (CollUtil.isEmpty(dictService.filterOutInvalidType(Lists.newArrayList(putDownDto.getType())))) {
            return ResultUtil.ok().putData(new ArrayList<>());
        }
        LambdaQueryWrapper<DictItemModel> wrapper = Wrappers.lambdaQuery();
        wrapper.select(DictItemModel::getType, DictItemModel::getCode, DictItemModel::getLabel);
        wrapper.eq(DictItemModel::getType, putDownDto.getType());
        wrapper.eq(DictItemModel::getIsInvalid, false);
        wrapper.eq(ObjectUtil.isNotEmpty(putDownDto.getCode()), DictItemModel::getCode, putDownDto.getCode());
        if (CollUtil.isNotEmpty(putDownDto.getExcludeCodes())) {
            wrapper.notIn(DictItemModel::getCode, putDownDto.getExcludeCodes());
        }
        if (CollUtil.isNotEmpty(putDownDto.getRangeCodes())) {
            wrapper.in(DictItemModel::getCode, putDownDto.getRangeCodes());
        }
        wrapper.orderByAsc(DictItemModel::getIndexSeq, DictItemModel::getCreatedAt);
        List<DictItemModel> results = super.list(wrapper);
        return ResultUtil.ok().putData(BeanUtil.toBean(results, DictItemPutDownVo.class));
    }

    /**
     * @see DictItemService#selectDictPutDownBatch(DictItemPutDownBatchDto)
     */
    @Override
    public ResultResponse<Map<String, List<DictItemPutDownVo>>> selectDictPutDownBatch(DictItemPutDownBatchDto putDownBatchDto) {
        Map<String, List<DictItemPutDownVo>> resultTypeItems = Maps.newHashMap();
        if (CollUtil.isEmpty(putDownBatchDto.getTypeCodes()) && CollUtil.isEmpty(putDownBatchDto.getSpecialTypeCodes())) {
            return ResultUtil.ok().putData(resultTypeItems);
        }
        List<String> validTypes = dictService.filterOutInvalidType(Lists.newArrayList(putDownBatchDto.getTypeCodes()));
        LambdaQueryWrapper<DictItemModel> wrapper = Wrappers.lambdaQuery();
        wrapper.select(DictItemModel::getType, DictItemModel::getCode, DictItemModel::getLabel);
        if (CollUtil.isNotEmpty(validTypes)) {
            wrapper.in(DictItemModel::getType, validTypes);
            wrapper.eq(DictItemModel::getIsInvalid, false);
            wrapper.orderByAsc(DictItemModel::getType, DictItemModel::getIndexSeq, DictItemModel::getCreatedAt);
            List<DictItemModel> results = super.list(wrapper);
            if (CollUtil.isNotEmpty(results)) {
                results.forEach(item -> {
                    List<DictItemPutDownVo> items = resultTypeItems.get(item.getType());
                    if (items == null) {
                        items = Lists.newArrayList();
                        items.add(BeanUtil.toBean(item, DictItemPutDownVo.class));
                        resultTypeItems.put(item.getType(), items);
                    } else {
                        items.add(BeanUtil.toBean(item, DictItemPutDownVo.class));
                    }
                });
            }
        }
        if (CollUtil.isNotEmpty(putDownBatchDto.getSpecialTypeCodes())) {
            putDownBatchDto.getSpecialTypeCodes().forEach(item -> {
                if (!validTypes.contains(item.getType())) {
                    ResultResponse<List<DictItemPutDownVo>> response = selectDictPutDown(item);
                    if (CollUtil.isNotEmpty(response.getResult())) {
                        resultTypeItems.put(item.getType(), response.getResult());
                    }
                }
            });
        }
        return ResultUtil.ok().putData(resultTypeItems);
    }

    /**
     * @see DictItemService#saveDictItem(DictItemCreateDto)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @LockAction(keys = {"#createDto.type", "#createDto.code"})
    public boolean saveDictItem(DictItemCreateDto createDto) {
        if (!dictService.existDictType(createDto.getType(), null)) {
            throw new BusinessException("常量类型不存在");
        }
        //检查数据字典项目是否存在
        if (existDictItem(createDto.getType(), createDto.getCode(), null)) {
            throw new BusinessException("常量项目已存在");
        }
        if (!this.save(BeanUtil.toBean(createDto, DictItemModel.class))) {
            throw new BusinessException("常量项目创建失败");
        }
        return true;
    }

    /**
     * @see DictItemService#updateDictItem(DictItemUpdateDto)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @LockAction(keys = {"#updateDto.type", "#updateDto.code"})
    public boolean updateDictItem(DictItemUpdateDto updateDto) {
        DictItemModel dictItem = getById(updateDto.getDictItemId());
        if (dictItem == null) {
            throw new BusinessException("常量项目不存在");
        }
        if (!StrUtil.equals(updateDto.getType(), dictItem.getType()) &&
                !dictService.existDictType(updateDto.getType(), null)) {
            throw new BusinessException("常量类型不存在");
        }
        if (existDictItem(updateDto.getType(), updateDto.getCode(), dictItem.getDictItemId())) {
            throw new BusinessException("常量项目已存在");
        }
        BeanUtil.copyProperties(updateDto, dictItem);
        if (!this.updateById(dictItem)) {
            throw new BusinessException("常量项目更新失败");
        }
        return true;
    }

    /**
     * @see DictItemService#enabledDictItem(Serializable, boolean)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean enabledDictItem(Serializable id, boolean isInvalid) {
        DictItemModel dictItem = getById(id);
        if (dictItem == null) {
            throw new BusinessException("常量项目不存在，更新失败");
        }
        if (isInvalid && dictService.isSystemDictType(dictItem.getType())) {
            throw new BusinessException("系统常量类型，不允许设置有效性");
        }
        dictItem.setIsInvalid(isInvalid);
        return this.updateById(dictItem);
    }

    /**
     * @see DictItemService#delDictItem(Serializable)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delDictItem(Serializable id) {
        DictItemModel dictItem = getById(id);
        if (dictItem == null) {
            throw new BusinessException("常量项目不存在");
        }
        if (dictService.isSystemDictType(dictItem.getType())) {
            throw new BusinessException("系统常量项目不允许删除");
        }
        return super.removeById(id);
    }

    /**
     * @see DictItemService#delDictItemByType(String)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delDictItemByType(String type) {
        LambdaQueryWrapper<DictItemModel> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(DictItemModel::getType, type);
        return super.remove(wrapper);
    }

    /**
     * @see DictItemService#updateDictTypeByType(String, String)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDictTypeByType(String fromDictType, String toDictType) {
        LambdaUpdateWrapper<DictItemModel> wrapper = Wrappers.lambdaUpdate();
        wrapper.eq(DictItemModel::getType, fromDictType);
        wrapper.set(DictItemModel::getType, toDictType);
        return this.update(wrapper);
    }

    /**
     * @see DictItemService#getDictLabels(List<String>)
     */
    @Override
    public Map<String, List<String>> getDictLabels(@NotNull List<String> dictTypes) {
        Map<String, List<String>> labelMaps = Maps.newHashMap();
        if (CollUtil.isEmpty(dictTypes)) {
            return labelMaps;
        }
        LambdaQueryWrapper<DictItemModel> wrapper = Wrappers.lambdaQuery();
        wrapper.in(DictItemModel::getType, dictTypes);
        wrapper.select(DictItemModel::getType, DictItemModel::getLabel);
        wrapper.orderByAsc(DictItemModel::getType);
        wrapper.orderByAsc(DictItemModel::getIndexSeq);
        wrapper.orderByDesc(DictItemModel::getCreatedAt);
        List<DictItemModel> items = this.list(wrapper);
        if (CollUtil.isNotEmpty(items)) {
            for (DictItemModel itemModel : items) {
                List<String> labels = labelMaps.get(itemModel.getType());
                if (CollUtil.isEmpty(labels)) {
                    labels = Lists.newArrayList();
                    labelMaps.put(itemModel.getType(), labels);
                }
                labels.add(itemModel.getLabel());
            }
        }
        return labelMaps;
    }

    @Override
    public List<DictItemVo> loadByType(String type) {
        List<DictItemModel> pos = this.lambdaQuery().eq(DictItemModel::getType, type).orderByAsc(DictItemModel::getIndexSeq).list();
        return BeanUtil.toBean(pos, DictItemVo.class);
    }

    /**
     * 根据类型和字典编码，判断是否存在数据项明细
     *
     * @param dictType 数据项类型
     * @param dictCode 类型项编码
     * @return
     */
    private boolean existDictItem(@NotNull String dictType, @NotNull String dictCode, String itemId) {
        LambdaQueryWrapper<DictItemModel> wrapper = Wrappers.lambdaQuery();
        wrapper.ne(StrUtil.isNotBlank(itemId), DictItemModel::getDictItemId, itemId);
        wrapper.eq(DictItemModel::getType, dictType);
        wrapper.eq(DictItemModel::getCode, dictCode);
        return SqlHelper.retBool(this.count(wrapper));
    }

}
