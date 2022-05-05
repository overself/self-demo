package com.hanwj.design.state.example;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StoppingState extends LiftState{
    public StoppingState(Lift lift) {
        super(lift);
        this.setStateName("电梯停止状态");
    }
    @Override
    public void open() {
        // 执行执行运行动作
        // 1、转化为打开状态
        mLift.setState(mLift.getOpeningState());
        // 2、开门
        mLift.open();
    }

    @Override
    public void close() {
        log.error("电梯关门");
    }

    @Override
    public void run() {
        // 运行动作
        // 1、运行状态
        this.mLift.setState(mLift.getRunningState());
        // 2、运行动作
        this.mLift.run();
    }

    @Override
    public void stop() {
        log.info("电梯已经停止，门要开。");
    }
}
