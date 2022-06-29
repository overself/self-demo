package com.wenjay.demo.app2;

import com.wenjay.framework.swagger.annotation.EnableWenjaySwagger2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableWenjaySwagger2
@EnableFeignClients(basePackages = "com.wenjay.demo.*")
public class StartDemo2Application {
    public static void main(String[] args) {
        SpringApplication.run(StartDemo2Application.class, args);
    }

}
