package com.hanwj.design.state.example;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OpeningState extends LiftState{

    public OpeningState(Lift lift) {
        super(lift);
        this.setStateName("电梯门开状态");
    }

    @Override
    public void open() {
        log.info("电梯门已经开启");
    }

    @Override
    public void close() {
        // 执行关门动作
        // 1、转化为关门状态
        mLift.setState(mLift.getClosingState());
        // 2、关门
        mLift.close();
    }

    @Override
    public void run() {
        log.error("开门状态，电梯不能运行动作");
    }

    @Override
    public void stop() {
        log.error("开门状态，电梯不能执行关门动作");
    }
}
