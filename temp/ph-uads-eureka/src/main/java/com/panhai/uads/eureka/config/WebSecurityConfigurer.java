/*
 * ******************************************************************************
 *  * Project Key : UADS
 *  * Create on 2018/11/01
 *  * Copyright (c) 2018-2099, 磐海数据有限公司版权所有.
 *  * 注意：本内容仅限于磐海数据有限公司内部传阅，禁止外泄以及用于其他的商业目的
 *  *****************************************************************************
 */
package com.panhai.uads.eureka.config;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * <p>Title: Eureka注册中服务安全校验策略</p>
 * <p> Description: Spring Cloud 2.0 以上的security默认启用了csrf检验，</p>
 * <p> 需要在eurekaServer端配置security的csrf检验为false。</p>
 * @author developer
 * @email developer@uads.com
 * @date 2018/11/01
 */
@EnableWebSecurity
public class WebSecurityConfigurer extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		//SpringCloud2 默认启动csrf安全校验
		//eureka 配置spring.security后其他服务无法连接注册中心
		http.csrf().disable();
		http.authorizeRequests().antMatchers("/actuator/**").permitAll();
		//http.authorizeRequests().anyRequest().authenticated().and().formLogin().and().httpBasic();
		super.configure(http);
	}

}
