package com.hanwj.design.observer;

import java.util.ArrayList;
import java.util.List;

public abstract class Subject<T extends TopicMessage> {

    /**
     * 用来保存注册的观察者对象，也就是报纸的订阅者
     */
    protected List<Observer> observers = new ArrayList<>();

    /**
     * 观察者注册订阅该主题
     *
     * @param observer 订阅者
     */
    public void attach(Observer observer){
        observers.add(observer);
    }

    /**
     * 观察者取消注册订阅该主题
     *
     * @param observer
     */
    public void detach(Observer observer) {
        observers.remove(observer);
    }

    /**
     * 观察者订阅的消息
     * 观察者与被观察者之间的消息传递对象
     * @param message
     */
    public void putMessage(T message) {
        notifyObservers(message);
    }

    /**
     * 通知观察者，让他们知道当前的状态
     */
    public abstract void notifyObservers(T message);
}
