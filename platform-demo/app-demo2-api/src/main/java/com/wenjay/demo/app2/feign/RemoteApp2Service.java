package com.wenjay.demo.app2.feign;

import com.wenjay.demo.app2.feign.factory.RemoteApp2ServiceFactory;
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
@FeignClient(value = "app-demo2-impl", fallbackFactory = RemoteApp2ServiceFactory.class)
public interface RemoteApp2Service {


    /**
     * 数据对象详细
     *
     * @param id 用户名
     * @return R
     */
    @GetMapping("api/app2/common/get/app-name/{id}")
    Result<Map<String, Object>> getAppName(@PathVariable("id") Integer id);

}
