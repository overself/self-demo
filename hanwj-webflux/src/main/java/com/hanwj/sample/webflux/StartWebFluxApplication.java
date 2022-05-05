package com.hanwj.sample.webflux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class StartWebFluxApplication {

    public static void main(String[] args) {
        SpringApplication.run(StartWebFluxApplication.class, args);
    }
}
