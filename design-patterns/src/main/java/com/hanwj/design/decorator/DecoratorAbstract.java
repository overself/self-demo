package com.hanwj.design.decorator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class DecoratorAbstract implements Component {

    private Component component = null;

    public DecoratorAbstract (Component component){
        this.component = component;
    }

    @Override
    public void printDescription(){
        this.component.printDescription();
    }
}
