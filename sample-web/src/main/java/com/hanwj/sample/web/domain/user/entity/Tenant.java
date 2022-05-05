package com.hanwj.sample.web.domain.user.entity;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hanwj.data.mysql.entity.BaseEntity;
import com.hanwj.sample.web.model.user.enums.TenantStatusEnum;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 租户
 *
 * @author developer
 */
@Data
@ApiModel(value = "租户信息")
@EqualsAndHashCode(callSuper = true)
@TableName("sys_tenant")
public class Tenant extends BaseEntity {

	private static final long serialVersionUID = 1L;
	/**
	 * 租户id
	 */
	@TableId(type = IdType.ASSIGN_ID)
	private String id;
	/**
	 * 电话
	 */
	private String phone;
	/**
	 * 租户名称
	 */
	private String name;
	/**
	 * 密码
	 */
	private String password;
	/**
	 * 账号编码（数据库字段）
	 */
	private String accountCode;
	/**
	 * 账号编码（加密逻辑属性）
	 */
	//private String account;
	/**
	 * 租户编号
	 */
	private String code;
	/**
	 * 开始时间
	 */
	private LocalDate startTime;
	/**
	 * 结束时间
	 */
	private LocalDate endTime;
	/**
	 * 租户冻结标记,9:冻结,0:正常
	 */
	private TenantStatusEnum status;

	@Override
	protected Serializable pkVal() {
		return id;
	}

	@Override
	protected List<Map<String, Object>> ukVal() {
		Map<String,Object> uk = new HashMap();
		uk.put("code",this.code);
		return CollUtil.newArrayList(uk);
	}
}
