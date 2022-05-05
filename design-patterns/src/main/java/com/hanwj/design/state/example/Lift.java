package com.hanwj.design.state.example;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Lift {

    //定义出电梯的所有状态
    private LiftState openingState;
    private LiftState closingState;
    private LiftState runningState;
    private LiftState stoppingState;

    // 定义当前电梯状态
    private LiftState mCurState;

    /**
     * 构造方法
     */
    public Lift() {
        openingState = new OpeningState(this);
        closingState = new ClosingState(this);
        runningState = new RunningState(this);
        stoppingState = new StoppingState(this);
    }

    /**
     * 显示当前运行状态
     */

    /**
     * 执行开门动作
     */
    public void open() {
        mCurState.open();
    }

    /**
     * 执行关门动作
     */
    public void close() {
        mCurState.close();
    }

    /**
     * 执行运行动作
     */
    public void run() {
        mCurState.run();
    }

    /**
     * 执行停止动作
     */
    public void stop() {
        mCurState.stop();
    }

    /**
     * 执行停止动作
     */
    public void displayState() {
        log.info(mCurState.getStateName());
    }

    // ##################设置当前电梯状态#####################

    /**
     * 设置当前电梯状态
     *
     * @param state
     */
    public void setState(LiftState state) {
        this.mCurState = state;
    }

    // ###################获取电梯的全部状态####################

    public LiftState getOpeningState() {
        return openingState;
    }

    public LiftState getClosingState() {
        return closingState;
    }

    public LiftState getRunningState() {
        return runningState;
    }

    public LiftState getStoppingState() {
        return stoppingState;
    }
}
