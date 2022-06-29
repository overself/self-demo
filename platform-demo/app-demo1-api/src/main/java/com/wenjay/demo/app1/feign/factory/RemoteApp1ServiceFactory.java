package com.wenjay.demo.app1.feign.factory;

import com.wenjay.demo.app1.feign.RemoteApp1Service;
import com.wenjay.demo.app1.feign.fallback.RemoteApp1ServiceImpl;
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
public class RemoteApp1ServiceFactory implements FallbackFactory<RemoteApp1Service> {

    @Override
    public RemoteApp1Service create(Throwable throwable) {
        RemoteApp1ServiceImpl fallback = new RemoteApp1ServiceImpl();
        fallback.setCause(throwable);
        return fallback;
    }
}
