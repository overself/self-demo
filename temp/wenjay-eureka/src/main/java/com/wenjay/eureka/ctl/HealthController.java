package com.wenjay.eureka.ctl;

import com.wenjay.eureka.config.AppWenjayContainerInfo;
import com.wenjay.eureka.euprovider.HealthStatusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class HealthController {

    @Autowired
    private HealthStatusService service;

    @Autowired
    private AppWenjayContainerInfo appInfo;

    @GetMapping("/health/set/status")
    public String putHealth(@RequestParam("status") boolean status) {
        service.setStatus(status);
        return "success";
    }

    @GetMapping("/health/get/status")
    public String getHealth() {
        return service.getStatus();
    }

    @GetMapping(path = "/health/get/info", produces = {MediaType.APPLICATION_JSON_UTF8_VALUE})
    public AppWenjayContainerInfo getAppInfo() {
        log.info("APP-INFO：{}", appInfo);
        return appInfo;
    }

}
