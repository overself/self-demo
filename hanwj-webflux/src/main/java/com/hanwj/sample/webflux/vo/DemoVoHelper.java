package com.hanwj.sample.webflux.vo;

import cn.hutool.core.lang.Snowflake;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class DemoVoHelper {

    private static final List<String> listVal = Lists.newArrayList("value1", "Value2");

    private static String activate;
    private static String environment;
    public DemoVoHelper(@Autowired Environment environ){
        activate = environ.getProperty("test.activate");
        environment = environ.getProperty("test.myEnv");
    }

    public static DemoVo getDemo() {
        try {
            //simulate io
            log.info("simulate start");
            TimeUnit.SECONDS.sleep(5);
            log.info("simulate end");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return DemoVo.builder().id(new Snowflake(1, 2).nextIdStr()).activate(activate).
                environment(environment).intVal(100).dateTime(LocalDateTime.now()).listVal(listVal).build();
    }

    public static List<DemoVo> getDemoAll() {
        List<DemoVo> demoVos = Lists.newArrayList();
        demoVos.add(DemoVo.builder().id(new Snowflake(1, 2).nextIdStr()).activate(activate).
                environment(environment).intVal(100).dateTime(LocalDateTime.now()).listVal(listVal).build());
        demoVos.add(DemoVo.builder().id(new Snowflake(2, 2).nextIdStr()).activate(activate).
                environment(environment).intVal(200).dateTime(LocalDateTime.now()).listVal(listVal).build());
        demoVos.add(DemoVo.builder().id(new Snowflake(3, 2).nextIdStr()).activate(activate).
                environment(environment).intVal(300).dateTime(LocalDateTime.now()).listVal(listVal).build());
        return demoVos;
    }
}
