/*
 * ******************************************************************************
 *  * Project Key : UADS
 *  * Create on 2018/11/01
 *  * Copyright (c) 2018-2099, 磐海数据有限公司版权所有.
 *  * 注意：本内容仅限于磐海数据有限公司内部传阅，禁止外泄以及用于其他的商业目的
 *  *****************************************************************************
 */
package com.panhai.uads.eureka;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * <p>Title: Eureka服务注册中服务</p>
 * <p> Description: Spring Cloud 2.0 以上的security默认启用了csrf检验，</p>
 * <p> 需要在eurekaServer端配置security的csrf检验为false。</p>
 * @author developer
 * @email developer@uads.com
 * @date 2018/11/01
 */
@EnableEurekaServer
@SpringBootApplication
public class UadsEurekaApplication {

	public static void main(String[] args) {
		SpringApplication.run(UadsEurekaApplication.class, args);
	}
}
