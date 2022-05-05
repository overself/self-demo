package com.hanwj.design.decorator;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class ConcreteDecorator extends DecoratorAbstract {

    private String name;

    public ConcreteDecorator(String name, Component component) {
        super(component);
        this.name = name;
    }

    // 定义自己的修饰逻辑
    public void printDescription() {
        log.info("我先自介绍一下，我叫{}, 我的特点如下：", showName());
        super.printDescription();
    }

    public String showName() {
        return this.name;
    }
}
