package com.hanwj.sample.web.domain.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hanwj.sample.web.domain.user.entity.Tenant;
import com.hanwj.sample.web.model.user.co.TenantCo;
import com.hanwj.sample.web.model.user.dto.TenantDto;
import com.hanwj.web.model.co.PageCondition;

/**
 * 租户管理
 *
 * @author lengleng
 * @date 2019-05-15 15:55:41
 */
public interface TenantService extends IService<Tenant> {

	/**
	 * 分页查询
	 *
	 * @param pageCo 分页查询条件
	 * @return IPage
	 */
	IPage<Tenant> pageList(PageCondition<TenantCo> pageCo);

	/**
	 * 分页查询
	 *
	 * @param pageCo 分页查询条件
	 * @return IPage
	 */
	IPage<Tenant> pageListCustom(PageCondition<TenantCo> pageCo);

	/**
	 * 保存租户
	 *
	 * @param tenant 租户
	 */
	boolean saveTenant(Tenant tenant);

	/**
	 * 保存租户
	 *
	 * @param tenant 租户
	 */
	boolean updateTenant(TenantDto tenantDto);

	/**
	 * 根据ID获得租户
	 * @param tenantId
	 * @return
	 */
	Tenant loadTenantById(String tenantId);

	/**
	 * 更新租户
	 * @param tenant
	 * @return
	 */
	Tenant updateTenantById(Tenant tenant);

	/**
	 * 删除租户
	 * @param tenantId
	 * @return
	 */
	boolean deleteTenantById(String tenantId);
}
