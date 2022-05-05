package com.hanwj.design.subpub;

/**
 * 发布者实现类
 * @param <M>
 */
public class PublisherOne<M> implements IPublisher<M> {

    private String name;

    public PublisherOne(String name) {
        super();
        this.name = name;
    }

    @Override
    public void publish(SubscribePublish subPub, M message, boolean isInstant) {
        subPub.publish(this.name, message, isInstant);
    }
}
