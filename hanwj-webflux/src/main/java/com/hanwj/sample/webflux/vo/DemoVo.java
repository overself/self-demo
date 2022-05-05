package com.hanwj.sample.webflux.vo;

import com.hanwj.web.model.vo.BaseVo;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class DemoVo extends BaseVo {

    private String id;

    private String activate;

    private String environment;

    private Integer intVal;

    private LocalDateTime dateTime;

    private List<String> listVal;

}
