package com.wenjay.demo.app2.feign.fallback;

import com.wenjay.demo.app2.feign.RemoteApp2Service;
import com.wenjay.framework.web.model.vo.Result;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * <p>Title: </p>
 * <p> Description:</p>
 * <p>  </p>
 *
 * @author HanWenjie
 */
@Slf4j
@Component
public class RemoteApp2ServiceImpl implements RemoteApp2Service {

    @Setter
    private Throwable cause;

    @Override
    public Result<Map<String, Object>> getAppName(@PathVariable("id") Integer id) {
        log.error("feign Remote-App2 请求失败:{}", id, cause);
        return Result.failed(cause.getMessage());
    }
}
