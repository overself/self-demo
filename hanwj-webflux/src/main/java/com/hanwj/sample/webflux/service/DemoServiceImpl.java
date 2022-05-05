package com.hanwj.sample.webflux.service;

import cn.hutool.core.util.StrUtil;
import com.hanwj.sample.webflux.vo.DemoVo;
import com.hanwj.sample.webflux.vo.DemoVoHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Service
public class DemoServiceImpl implements DemoService<DemoVo> {

    @Autowired
    private DemoService<DemoVo> self;

    @Override
    public boolean create(DemoVo obj) {
        log.info("创建对象：{}", obj);
        return true;
    }

    @Override
    public DemoVo getDemo(String objId) {
        log.info("获得对象：{}", objId);
        DemoVo demoVo = DemoVoHelper.getDemo();
        if (StrUtil.isNotBlank(objId)) {
            demoVo.setId(objId);
        }
        return demoVo;
    }

    @Override
    public Mono<DemoVo> getMonoDemo(String objId) {
        log.info("获得对象：{}", objId);
        return Mono.justOrEmpty(self.getDemo(objId));
    }

    @Override
    public List<DemoVo> getAll() {
        log.info("获得对象列表");
        return DemoVoHelper.getDemoAll();
    }

    @Override
    public Flux<DemoVo> getFluxAll() {
        log.info("获得对象列表");
        List<DemoVo> list = self.getAll();
        return Flux.fromIterable(list);
    }
}
