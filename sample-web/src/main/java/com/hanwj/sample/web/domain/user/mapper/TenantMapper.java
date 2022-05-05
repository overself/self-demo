package com.hanwj.sample.web.domain.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hanwj.sample.web.domain.user.entity.Tenant;
import com.hanwj.sample.web.model.user.co.TenantCo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 租户
 *
 * @author developer
 */
@Mapper
public interface TenantMapper extends BaseMapper<Tenant> {

    Page<Tenant> selectTenantListByCustomSql(@Param("page") IPage<TenantCo> iPage, @Param("co") TenantCo co);

}
