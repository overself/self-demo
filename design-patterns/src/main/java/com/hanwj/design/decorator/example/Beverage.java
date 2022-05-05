package com.hanwj.design.decorator.example;

/**
 * 抽象组件角色：饮料
 */
public abstract class Beverage {

    String description = "Unknown Beverage";

    public String getDescription() {
        return description;
    }
    public abstract double cost();

}
