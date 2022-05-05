package com.hanwj.design.state;

import lombok.Data;

/**
 * 定义客户感兴趣的接口。维护一个ConcreteState子类的实例，这个实例定义当前状态。
 */
@Data
public class Context {

    /**
     * 状态
     */
    private State state;

    public Context(){
        state = null;
    }

}
