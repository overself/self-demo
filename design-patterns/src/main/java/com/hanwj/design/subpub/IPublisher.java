package com.hanwj.design.subpub;

/**
 * 发布者接口
 *
 * @param <M>
 */
public interface IPublisher<M> {

    /**
     * @param subPub    订阅器
     * @param message   消息
     * @param isInstant 是否立即发送
     * @Description: 向订阅器发布消息
     */
    void publish(SubscribePublish subPub, M message, boolean isInstant);

}
