package com.hanwj.design.decorator;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class ConcreteDecoratorJap extends DecoratorAbstract {

    private String name;

    public ConcreteDecoratorJap(String name, Component component) {
        super(component);
        this.name = name;
    }

    // 定义自己的修饰逻辑
    public void printDescription() {
        super.printDescription();
        log.info("其实我来自日本，我的日本名字是{}：", showName());
    }

    public String showName() {
        return this.name;
    }
}
