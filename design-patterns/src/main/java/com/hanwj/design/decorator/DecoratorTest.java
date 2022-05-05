package com.hanwj.design.decorator;

/**
 * https://www.jianshu.com/p/10a77d91c93f
 * https://blog.csdn.net/u010295735/article/details/89031858
 * https://www.cnblogs.com/volcano-liu/p/10897897.html
 */
public class DecoratorTest {

    public static void main(String[] args) {

        Component school = new ConcreteComponent();
        school.printDescription();
        System.out.println("------------------------");

        Component component = new ConcreteDecorator("测试001", school);
        component.printDescription();
        System.out.println("------------------------");
        Component component2 = new ConcreteDecoratorJap("测试002",component);
        component2.printDescription();


    }
}
