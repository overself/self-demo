package com.hanwj.design.state.example;

import lombok.Data;

/**
 * 定义电梯行为：打开、关闭、运行、停止
 */
@Data
public abstract class LiftState {

    private String stateName;

    //状态转换
    protected Lift mLift;

    /**
     * 通过构造函数引入电梯的实例化对象
     *
     * @param lift
     */
    public LiftState(Lift lift) {
        this.mLift = lift;
    }

    // 电梯门开状态
    public abstract void open();

    // 电梯关门状态
    public abstract void close();

    // 电梯移动状态
    public abstract void run();

    // 电梯停止状态
    public abstract void stop();
}
