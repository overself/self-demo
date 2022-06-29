package com.wenjay.demo.app1.feign;

import com.wenjay.demo.app1.feign.factory.RemoteApp1ServiceFactory;
import com.wenjay.framework.web.model.vo.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * <p>Title: </p>
 * <p> Description:</p>
 * <p>  </p>
 *
 * @author Hanwenjie
 */
@FeignClient(value = "app-demo1-impl", fallbackFactory = RemoteApp1ServiceFactory.class)
public interface RemoteApp1Service {


    /**
     * 数据对象详细
     *
     * @param id 用户名
     * @return R
     */
    @GetMapping("api/app1/common/get/app-name/{id}")
    Result<Map<String, Object>> getAppName(@PathVariable("id") Integer id);

}
