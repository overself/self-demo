package com.hanwj.design.state.example;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClosingState extends LiftState {

    public ClosingState(Lift lift) {
        super(lift);
        this.setStateName("电梯关门状态");
    }


    @Override
    public void open() {
        // 执行执行运行动作
        // 1、转化为开门状态
        mLift.setState(mLift.getOpeningState());
        // 2、开门
        mLift.open();
    }

    @Override
    public void close() {
        log.info("电梯门已经关闭");

    }

    @Override
    public void run() {
        // 执行执行运行动作
        // 1、转化为运行状态
        mLift.setState(mLift.getRunningState());
        // 2、运行
        mLift.run();
    }

    @Override
    public void stop() {
        // 停止动作
        // 1、转化为停止状态
        this.mLift.setState(mLift.getStoppingState());
        // 2、停止
        this.mLift.stop();
    }
}
