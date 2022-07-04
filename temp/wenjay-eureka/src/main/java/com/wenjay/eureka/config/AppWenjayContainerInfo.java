package com.wenjay.eureka.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "wenjay.parma")
public class AppWenjayContainerInfo {

    private String welcome;

    private String podhost;

    private String podport;

    private String podspace;

    private Map<String,String> labels = new HashMap<>();

}
