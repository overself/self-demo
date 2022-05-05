package com.hanwj.sample.web.controller;

import cn.hutool.core.util.StrUtil;
import com.hanwj.core.exception.BusinessException;
import com.hanwj.web.model.vo.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.jasypt.encryption.StringEncryptor;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = {"/api/common"})
@Api(value = "架构公共测试", tags = "架构公共测试")
public class CommonController {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(CommonController.class);
    @Value("${test.activate}")
    private String activate;
    @Value("${test.myEnv}")
    private String environment;
    @Value("${test.myProp:我是默认值}")
    private String myProp;
    @Autowired
    private StringEncryptor stringEncryptor;

    @PostMapping("/jasypt")
    @ApiOperation(value = "jasypt加密解密", notes = "jasypt加密解密")
    public Result<Map<String,String>> doEncryptor(@RequestBody Map<String, String> params) {
        log.info("测试属性：{}", myProp);
        if ("true".equalsIgnoreCase(params.get("isEncrypt"))) {
            String encrypt = stringEncryptor.encrypt(params.get("encrypt"));
            log.info("加密结果：ENC({})", encrypt);
            Map<String,String> result = new HashMap<>();
            result.put("activate", activate);
            result.put("environment", environment);
            result.put("myProp", myProp);
            result.put("encrypt", "ENC("+encrypt+")");
            return Result.ok(result);
        } else {
            String decryptVal = params.get("decrypt");
            if (!StrUtil.startWith(decryptVal, "ENC(") && !StrUtil.endWith(decryptVal, ")")) {
                throw new BusinessException("请输入正确的字符串ENC(XXXXXXXXXXXXXXXX)");
            }
            decryptVal = StrUtil.sub(decryptVal, 4, StrUtil.lastIndexOfIgnoreCase(decryptVal, ")"));
            decryptVal = stringEncryptor.decrypt(decryptVal);
            log.info("解密结果：{}", decryptVal);
            Map<String,String> result = new HashMap<>();
            result.put("activate", activate);
            result.put("environment", environment);
            result.put("myProp", myProp);
            result.put("decrypt", decryptVal);
            return Result.ok(result);
        }
    }
}
