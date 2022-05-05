package com.hanwj.design.decorator.example;

/**
 * 具体的实现：某一种咖啡
 */
public class DarkRoast extends Beverage {
    public DarkRoast() {
        description = "Dark Roast Coffee";
    }

    public double cost() {
        return .99;
    }
}
