package com.hanwj.design.state;

/**
 * 一个具体状态类(停止状态)
 * 每一子类实现一个与Context的一个状态相关的行为。
 */
public class StopState implements State {
    @Override
    public void doAction(Context context) {
        System.out.println("Player is in stop state");
        context.setState(this);
    }

    public String toString() {
        return "Stop State";
    }

}
