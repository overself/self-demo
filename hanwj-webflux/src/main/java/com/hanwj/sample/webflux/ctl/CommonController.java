package com.hanwj.sample.webflux.ctl;

import cn.hutool.core.util.StrUtil;
import com.hanwj.core.exception.BusinessException;
import com.hanwj.sample.webflux.service.DemoService;
import com.hanwj.sample.webflux.vo.DemoVo;
import com.hanwj.web.model.vo.Result;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = {"/api/common"})
public class CommonController {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(CommonController.class);

    @Autowired
    private DemoService<DemoVo> service;

    @GetMapping("/demo")
    public Result<DemoVo> getDemo() {
        log.info("测试属性");
        return Result.ok(service.getDemo(null));
    }

    @PostMapping("/mono/demo")
    public Result<DemoVo> postDemo(@RequestParam String id) {
        log.info("测试属性：{}", id);
        return Result.ok(service.getDemo(id));
    }

    @GetMapping("/error/{id}")
    public Result<DemoVo> checkErrorDemo(@PathVariable("id") final String id) {
        if(StrUtil.isBlank(id)) {
            throw new BusinessException();
        }
        return Result.ok(service.getDemo(id));
    }

    @GetMapping("/error/mono/{id}")
    public Mono<DemoVo> checkErrorMonoDemo(@PathVariable("id") final String id) {
        return Mono.justOrEmpty(StrUtil.isBlank(id) ? null : service.getDemo(id))
                .switchIfEmpty(Mono.error(BusinessException::new));
    }

    @PostMapping("/mono/demo1")
    public Mono<Result<DemoVo>> getMonoDemo1(@RequestBody Map<String, String> params) {
        log.info("测试属性：{}", params);
        DemoVo result = service.getDemo(params.get("id"));
        return Mono.justOrEmpty(Result.ok(result));
    }

    @PostMapping("/mono/demo2")
    public Mono<DemoVo> getMonoDemo2(@RequestBody Map<String, String> params) {
        log.info("测试属性：{}", params);
        Mono<DemoVo> result = service.getMonoDemo(params.get("id"));
        return result;
    }

    @PostMapping("/mono/demo3")
    public Mono<Result<List<DemoVo>>> myMonoDemo3(@RequestBody Map<String, String> params) {
        log.info("测试属性：{}", params);
        List<DemoVo> result = service.getFluxAll().collectList().block();
        return Mono.justOrEmpty(Result.ok(result));
    }

    @PostMapping("/mono/demo4")
    public Mono<List<DemoVo>> myMonoDemo4(@RequestBody Map<String, String> params) {
        log.info("测试属性：{}", params);
        return service.getFluxAll().collectList();
    }

    @PostMapping("/flux/demo4")
    public Flux<DemoVo> myMonoDemo5(@RequestBody Map<String, String> params) {
        log.info("测试属性：{}", params);
        return service.getFluxAll();
    }

}
