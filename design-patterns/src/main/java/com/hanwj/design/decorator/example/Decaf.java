package com.hanwj.design.decorator.example;

/**
 * 具体的实现：某一种咖啡
 */
public class Decaf extends Beverage {
    public Decaf() {
        description = "Decaf Coffee";
    }

    public double cost() {
        return 1.05;
    }
}
