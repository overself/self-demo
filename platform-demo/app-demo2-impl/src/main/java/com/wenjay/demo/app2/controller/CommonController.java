package com.wenjay.demo.app2.controller;

import com.google.common.collect.Maps;
import com.wenjay.demo.app1.feign.RemoteApp1Service;
import com.wenjay.framework.web.model.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping(value = {"api/app2/common"})
public class CommonController {

    @Value("${spring.application.name}")
    private String appName;

    @Autowired
    private RemoteApp1Service app1Service;

    @GetMapping("/get/app-name/{id}")
    public Result<Map<String, Object>> getAppName(@PathVariable("id") Integer id) {
        Map<String, Object> result = Maps.newHashMap();
        result.put("id", id);
        result.put("appName", appName);
        return Result.ok(result);
    }

    @GetMapping("/test/app-feign/{id}")
    public Result<Map<String, Object>> testFeignAppName(@PathVariable("id") Integer id) {
        Result<Map<String, Object>> result = app1Service.getAppName(id);
        log.info("result：{}-{}", result.getCode(), result.getMsg());
        return Result.ok(result.getResult());
    }
}
