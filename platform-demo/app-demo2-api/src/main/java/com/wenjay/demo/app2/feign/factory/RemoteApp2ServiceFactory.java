package com.wenjay.demo.app2.feign.factory;

import com.wenjay.demo.app2.feign.RemoteApp2Service;
import com.wenjay.demo.app2.feign.fallback.RemoteApp2ServiceImpl;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * <p>Title: </p>
 * <p> Description:</p>
 * <p>  </p>
 *
 * @author HanWenjie
 */
@Component
public class RemoteApp2ServiceFactory implements FallbackFactory<RemoteApp2Service> {

    @Override
    public RemoteApp2Service create(Throwable throwable) {
        RemoteApp2ServiceImpl fallback = new RemoteApp2ServiceImpl();
        fallback.setCause(throwable);
        return fallback;
    }
}
