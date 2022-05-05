package com.hanwj.design.subpub;

/**
 * 订阅者接口
 *
 * @param <M>
 */
public interface ISubcriber<M> {

    /**
     * @Description: 订阅
     * @param: subPub 订阅器
     */
    void subcribe(SubscribePublish subPub);

    /**
     * @Description: 退订
     * @param: subPub 订阅器
     */
    void unSubcribe(SubscribePublish subPub);

    /**
     * @Description: 接收消息
     * @param: publisher 发布者
     * @param: message 消息
     */
    void update(String publisher, M message);

}
