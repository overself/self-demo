package com.hanwj.design.state;

/**
 * 一个具体状态类(启动状态)
 * 每一子类实现一个与Context的一个状态相关的行为。
 */
public class StartState implements State{

    @Override
    public void doAction(Context context) {
        System.out.println("Player is in start state");
        context.setState(this);
    }

    public String toString(){
        return "Start State";
    }
}
