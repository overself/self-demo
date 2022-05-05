package com.hanwj.design.decorator;

import lombok.extern.slf4j.Slf4j;

/**
 * 组件的一个具体实现，或者基础构件
 */
@Slf4j
public class ConcreteComponent implements Component {

    @Override
    public void printDescription() {
        log.info("我只是一次最简单的实现！");
    }

}
