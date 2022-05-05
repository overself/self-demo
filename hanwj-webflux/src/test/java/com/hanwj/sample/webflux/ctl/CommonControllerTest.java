package com.hanwj.sample.webflux.ctl;

import com.hanwj.web.model.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RunWith(SpringRunner.class)
@WebFluxTest(controllers = CommonController.class)
@ComponentScan(basePackages = {"com.hanwj.sample.webflux.service"})
class CommonControllerTest {
    @Autowired
    private WebTestClient webTestClient;

    @Test
    public void getHello() {
        Map<String, String> map = new HashMap<>();
        map.put("name", "生产环境");
        WebTestClient.ResponseSpec response = webTestClient.get().uri("/api/common/demo").exchange();
        response.expectStatus().isOk();
        ParameterizedTypeReference<Result<Map<String, String>>> reference = new ParameterizedTypeReference<Result<Map<String, String>>>() {
        };
        WebTestClient.BodySpec result = response.expectBody(reference);
        log.info("{}", result);
    }
}
