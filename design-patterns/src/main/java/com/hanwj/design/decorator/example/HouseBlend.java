package com.hanwj.design.decorator.example;

/**
 * 具体的实现：某一种咖啡
 */
public class HouseBlend  extends Beverage {
    public HouseBlend() {
        description = "House Blend Coffee";
    }

    public double cost() {
        return .89;
    }
}
