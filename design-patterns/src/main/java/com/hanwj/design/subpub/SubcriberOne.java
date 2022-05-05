package com.hanwj.design.subpub;

import lombok.extern.slf4j.Slf4j;

/**
 * 订阅者实现类
 * @param <M>
 */
@Slf4j
public class SubcriberOne<M> implements ISubcriber<M> {

    public String name;

    public SubcriberOne(String name) {
        super();
        this.name = name;
    }

    @Override
    public void subcribe(SubscribePublish subPub) {
        subPub.subcribe(this);

    }

    @Override
    public void unSubcribe(SubscribePublish subPub) {
        subPub.unSubcribe(this);
    }

    @Override
    public void update(String publisher, M message) {
        log.info(this.name + " 收到 " + publisher + "发来的消息：" + message.toString());
    }
}
