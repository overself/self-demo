package com.hanwj.design.decorator.example;

/**
 * 具体的实现：某一种咖啡
 */
public class Espresso extends Beverage {

    public Espresso() {
        description = "Espresso";
    }

    public double cost() {
        return 1.99;
    }
}
