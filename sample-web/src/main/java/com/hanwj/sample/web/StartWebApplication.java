package com.hanwj.sample.web;

import com.hanwj.swagger.annotation.EnableHwjSwagger2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableHwjSwagger2
@SpringBootApplication
public class StartWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(StartWebApplication.class, args);
	}

}
