package com.hanwj.sample.web.domain.user.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hanwj.core.common.constant.MessageCode;
import com.hanwj.core.exception.BusinessException;
import com.hanwj.core.util.BeanUtils;
import com.hanwj.sample.web.domain.user.entity.Tenant;
import com.hanwj.sample.web.domain.user.mapper.TenantMapper;
import com.hanwj.sample.web.domain.user.service.TenantService;
import com.hanwj.sample.web.model.user.co.TenantCo;
import com.hanwj.sample.web.model.user.dto.TenantDto;
import com.hanwj.web.model.co.PageCondition;
import com.hanwj.web.model.co.SortItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class TenantServiceImpl extends ServiceImpl<TenantMapper, Tenant> implements TenantService {

    @Autowired
    private TenantService self;

    @Override
    public IPage<Tenant> pageList(PageCondition<TenantCo> pageCo) {
        if (CollUtil.isEmpty(pageCo.getSortItems())) {
            pageCo.addSortItem(new SortItem("create_time"));
        }
        IPage query = new Page();
        LambdaQueryWrapper<Tenant> wrapper = Wrappers.lambdaQuery();
        wrapper.like(StrUtil.isNotBlank(pageCo.getCondition().getName()),Tenant::getName,pageCo.getCondition().getName());
        wrapper.eq(StrUtil.isNotBlank(pageCo.getCondition().getPassword()),Tenant::getPassword,pageCo.getCondition().getPassword());
        return page(query, wrapper);
    }

    @Override
    public IPage<Tenant> pageListCustom(PageCondition<TenantCo> pageCo) {
        IPage<TenantCo> query = new Page<>();
        Page<Tenant> records = baseMapper.selectTenantListByCustomSql(query, pageCo.getCondition());
        return records;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveTenant(Tenant tenant) {
        return self.save(tenant);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateTenant(TenantDto tenantDto) {
        Tenant tenant = this.getById(tenantDto.getId());
        if (tenant==null) {
            throw new BusinessException(MessageCode.COMM_BASE_ERR0001);
        }
        BeanUtils.copyProperties(tenantDto,tenant);
        self.updateTenantById(tenant);
        return true;
    }

    //@Cacheable(value = CacheConstants.GLOBALLY+"Tenant#60", key = "#tenantId", unless = "#result == null")
    public Tenant loadTenantById(String tenantId){
        return this.getById(tenantId);
    }

    @Transactional(rollbackFor = Exception.class)
    //@CachePut(value = CacheConstants.GLOBALLY+"Tenant#60", key = "#tenant.id", unless = "#result == null")
    public Tenant updateTenantById(Tenant tenant){
        if(!self.updateById(tenant)){
            throw new BusinessException(MessageCode.COMM_BASE_ERR0003);
        }
        return tenant;
    }

    @Transactional(rollbackFor = Exception.class)
    //@CacheEvict(value = CacheConstants.GLOBALLY+"Tenant", key = "#tenantId")
    public boolean deleteTenantById(String tenantId){
        return self.removeById(tenantId);
    }

}

