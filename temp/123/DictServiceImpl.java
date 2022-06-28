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
import com.jjl.dxz.module.biz.workflow.base.dto.DictCreateDto;
import com.jjl.dxz.module.biz.workflow.base.dto.DictQueryDto;
import com.jjl.dxz.module.biz.workflow.base.dto.DictUpdateDto;
import com.jjl.dxz.module.biz.workflow.base.service.DictItemService;
import com.jjl.dxz.module.biz.workflow.base.service.DictService;
import com.jjl.dxz.module.biz.workflow.base.vo.DictVo;
import com.jjl.dxz.module.biz.workflow.entity.DictModel;
import com.jjl.dxz.module.biz.workflow.mapper.DictMapper;
import com.jjl.features.exception.BusinessException;
import com.jjl.features.web.model.PageCondition;
import com.jjl.features.web.model.Query;
import com.jjl.features.web.model.ResultResponse;
import com.jjl.features.web.util.ResultUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 常量定义服务实现类
 *
 * @author HanWenjie
 */
@Slf4j
@DubboService
public class DictServiceImpl extends ServiceImpl<DictMapper, DictModel> implements DictService {

    @DubboReference
    private DictItemService itemService;

    /**
     * @see DictService#selectDictPage(PageCondition)
     */
    @Override
    public ResultResponse<List<DictVo>> selectDictPage(PageCondition<DictQueryDto> condition) {
        Page<DictModel> page = new Query<>(condition);
        DictQueryDto conDict = condition.getCondition();
        if (conDict == null) {
            conDict = new DictQueryDto();
            condition.setCondition(conDict);
        }
        LambdaQueryWrapper<DictModel> wrapper = Wrappers.lambdaQuery();
        wrapper.like(StrUtil.isNotBlank(conDict.getType()), DictModel::getType, conDict.getType());
        wrapper.like(StrUtil.isNotBlank(conDict.getDescription()), DictModel::getDescription, conDict.getDescription());
        wrapper.eq(ObjectUtil.isNotNull(conDict.getIsInvalid()), DictModel::getIsInvalid, conDict.getIsInvalid());
        wrapper.eq(ObjectUtil.isNotNull(conDict.getIsSystem()), DictModel::getIsSystem, conDict.getIsSystem());
        wrapper.orderByAsc(DictModel::getType);
        IPage<DictModel> iPage = baseMapper.selectPage(page, wrapper);
        List<DictVo> dictVos = BeanUtil.toBean(iPage.getRecords(), DictVo.class);
        if (CollUtil.isNotEmpty(dictVos)) {
            List<String> types = dictVos.stream().map(DictVo::getType).collect(Collectors.toList());
            Map<String, String> labels = getDictRemarkString(types);
            dictVos.stream().forEach(item -> {
                if (StrUtil.isNotBlank(item.getDependencyType())) {
                    DictModel dict = getDictByType(item.getDependencyType());
                    item.setDependencyDesc(dict != null ? dict.getDescription() : "");
                }
                if (StrUtil.isBlank(item.getRemarks())) {
                    item.setRemarks(labels.get(item.getType()));
                }
            });
        }
        return ResultUtil.ok().putData(dictVos).withPageInfo(condition);
    }

    /**
     * @see DictService#saveDict(DictCreateDto)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @LockAction(keys = {"#dictCreateDto.type"})
    public boolean saveDict(DictCreateDto dictCreateDto) {
        if (existDictType(dictCreateDto.getType(), null)) {
            throw new BusinessException("常量已存在");
        }
        if (StrUtil.equals(dictCreateDto.getType(), dictCreateDto.getDependencyType())) {
            throw new BusinessException("常量不可以自关联");
        }
        //检查被依赖的常量是否存在
        if (StrUtil.isNotBlank(dictCreateDto.getDependencyType()) &&
                !existDictType(dictCreateDto.getDependencyType(), null)) {
            throw new BusinessException("依赖的常量不存在");
        }
        return super.save(BeanUtil.toBean(dictCreateDto, DictModel.class));
    }

    /**
     * @see DictService#updateDict(DictUpdateDto)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateDict(DictUpdateDto updateDto) {
        DictModel dictModel = getById(updateDto.getDictId());
        if (dictModel == null) {
            throw new BusinessException("常量不存在");
        }
        if (StrUtil.equals(updateDto.getType(), updateDto.getDependencyType())) {
            throw new BusinessException("常量不可以自关联");
        }
        //是否存在循环彼此依赖的现象
        if (StrUtil.isNotBlank(updateDto.getDependencyType()) &&
                hasDependencyDict(dictModel.getType(), updateDto.getDependencyType())) {
            throw new BusinessException("常量之间不可以循环彼此依赖");
        }
        if (!StrUtil.equals(dictModel.getType(), updateDto.getType())) {
            if (existDictType(dictModel.getType(), updateDto.getDictId())) {
                throw new BusinessException("常量已存在");
            }

            if (hasDependencyDict(dictModel.getType(), null) &&
                    !updateDependencyDictForType(dictModel.getType(), updateDto.getType())) {
                throw new BusinessException("常量更新失败");
            }
            //更新字典项目的类型
            itemService.updateDictTypeByType(dictModel.getType(), updateDto.getType());
        }
        //检查被依赖的常量是否存在
        BeanUtil.copyProperties(updateDto, dictModel, "dictId");
        if (!this.updateById(dictModel)) {
            throw new BusinessException("常量更新失败");
        }
        return true;
    }

    /**
     * @see DictService#enabledDict(Serializable, boolean)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean enabledDict(Serializable id, boolean isInvalid) {
        DictModel dictModel = getById(id);
        if (dictModel == null) {
            throw new BusinessException("常量更新失败");
        }
        if (isInvalid && dictModel.isSystem()) {
            throw new BusinessException("系统常量类型，不允许设置无效");
        }
        //检查被依赖的常量是否存在
        if (isInvalid && hasDependencyDict(dictModel.getType(), null)) {
            throw new BusinessException("该常量被依赖，不允许设置有效性");
        }
        dictModel.setIsInvalid(isInvalid);
        return this.updateById(dictModel);
    }

    /**
     * @see DictService#delDict(Serializable)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delDict(Serializable id) {
        DictModel dictModel = this.getById(id);
        if (dictModel == null) {
            throw new BusinessException("常量不存在");
        }
        if (dictModel.isSystem()) {
            throw new BusinessException("系统常量类型不允许删除");
        }
        //检查被依赖的常量是否存在
        if (hasDependencyDict(dictModel.getType(), null)) {
            throw new BusinessException("常量被引用，不允许删除");
        }
        //删除关联的常量项目
        itemService.delDictItemByType(dictModel.getType());
        if (!super.removeById(id)) {
            throw new BusinessException("常量删除失败");
        }
        return true;
    }

    /**
     * @see DictService#existDictType(String, String)
     */
    @Override
    public boolean existDictType(@NotNull String dictType, String dictId) {
        LambdaQueryWrapper<DictModel> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(DictModel::getType, dictType);
        wrapper.ne(StrUtil.isNotBlank(dictId), DictModel::getDictId, dictId);
        return SqlHelper.retBool(this.count(wrapper));
    }

    /**
     * @see DictService#isSystemDictType(String)
     */
    @Override
    public boolean isSystemDictType(@NotNull String dictType) {
        LambdaQueryWrapper<DictModel> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(DictModel::getType, dictType);
        wrapper.eq(DictModel::getIsSystem, true);
        return SqlHelper.retBool(this.count(wrapper));
    }

    /**
     * @see DictService#filterOutInvalidType(List<String>)
     */
    @Override
    public List<String> filterOutInvalidType(@NotNull List<String> dictType) {
        LambdaQueryWrapper<DictModel> wrapper = Wrappers.lambdaQuery();
        wrapper.select(DictModel::getType);
        wrapper.in(DictModel::getType, dictType);
        wrapper.eq(DictModel::getIsInvalid, false);
        List<DictModel> results = this.list(wrapper);
        return CollUtil.isEmpty(results) ? Lists.newArrayList() : results.stream().map(DictModel::getType).collect(Collectors.toList());
    }


    /**
     * @param dictTypes 数据项类型列表
     * @return
     */
    public Map<String, String> getDictRemarkString(List<String> dictTypes) {
        Map<String, String> labelMaps = Maps.newHashMap();
        Map<String, List<String>> labelMapList = itemService.getDictLabels(dictTypes);
        if (CollUtil.isEmpty(labelMapList)) {
            return labelMaps;
        }
        dictTypes.forEach(type -> {
            StringBuffer remarks = new StringBuffer();
            List<String> labelList = labelMapList.get(type);
            if (CollUtil.isNotEmpty(labelList)) {
                for (String item : labelList) {
                    if (remarks.length() < 50) {
                        if (remarks.length() > 0) {
                            remarks.append(",").append(item);
                        } else {
                            remarks.append(item);
                        }
                    } else {
                        remarks.append(" ...");
                        break;
                    }
                }
                labelMaps.put(type, remarks.toString());
            }
        });
        return labelMaps;
    }

    /**
     * 根据类型判断是否处于被依赖中状态
     *
     * @param dependencyDictType 检查是否被依赖的产量
     * @param currentType        价差当前类型是否依赖了dependencyDictType
     * @return 是否存在
     */
    private boolean hasDependencyDict(@NotNull String dependencyDictType, String currentType) {
        LambdaQueryWrapper<DictModel> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(DictModel::getDependencyType, dependencyDictType);
        wrapper.eq(StrUtil.isNotBlank(currentType), DictModel::getType, currentType);
        return SqlHelper.retBool(this.count(wrapper));
    }

    /**
     * 根据类型判断是否处于被依赖中状态
     *
     * @param dictType 常量类型
     * @return 是否存在
     */
    private DictModel getDictByType(@NotNull String dictType) {
        LambdaQueryWrapper<DictModel> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(DictModel::getType, dictType);
        return this.getOne(wrapper, false);
    }

    /**
     * 因为被依赖的常量类型编码变更，修改所有关联依赖的常量的子项
     *
     * @param fromDictType 常量类型
     * @param toDictType   常量类型
     * @return 被依赖的常量的子项是否修改成功
     */
    private boolean updateDependencyDictForType(String fromDictType, String toDictType) {
        LambdaUpdateWrapper<DictModel> wrapper = Wrappers.lambdaUpdate();
        wrapper.eq(DictModel::getDependencyType, fromDictType);
        wrapper.set(DictModel::getDependencyType, toDictType);
        return this.update(wrapper);
    }

}
