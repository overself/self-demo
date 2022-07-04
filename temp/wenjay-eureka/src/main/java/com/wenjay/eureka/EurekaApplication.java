/*
 * ******************************************************************************
 *  * Project Key :  ${projectName}
 *  * Create on 2018/11/01
 *  * Copyright (c) 2018-2099, ${company}版权所有.
 *  * 注意：本内容仅限于${company}内部传阅，禁止外泄以及用于其他的商业目的
 *  *****************************************************************************
 */
package com.wenjay.eureka;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * <p>Title: Eureka服务注册中心 启动服务</p>
 * <p> Description: bootstrap启动，</p>
 * @author developer
 * @email developer@wenjay.com
 * @date 2018/11/01
 */
@EnableEurekaServer
@SpringBootApplication
public class EurekaApplication {

	public static void main(String[] args) {
		SpringApplication.run(EurekaApplication.class, args);
	}
}
