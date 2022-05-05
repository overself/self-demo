package com.hanwj.design.state.example;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RunningState extends LiftState{
    public RunningState(Lift lift) {
        super(lift);
        this.setStateName("电梯运行状态");
    }

    @Override
    public void open() {
        log.error("运行状态，不能执行开门动作");
    }

    @Override
    public void close() {
        log.error("运行状态，不能执行关门动作");
    }

    @Override
    public void run() {
        log.info("电梯正在运行中");
    }

    @Override
    public void stop() {
        // 执行执行运行动作
        // 1、转化为停止状态
        mLift.setState(mLift.getStoppingState());
        // 2、停止
        mLift.stop();
    }
}
