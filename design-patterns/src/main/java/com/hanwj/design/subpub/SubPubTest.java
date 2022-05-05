package com.hanwj.design.subpub;

/**
 * 发布订阅测试类
 */
public class SubPubTest {

    public static void main(String[] args) {

        SubscribePublish<String> subPub = new SubscribePublish<String>("订阅器");
        IPublisher<String> publisher1 = new PublisherOne<String>("发布者1");
        ISubcriber<String> subcriber1 = new SubcriberOne<String>("订阅者1");
        ISubcriber<String> subcriber2 = new SubcriberOne<String>("订阅者2");

        subcriber1.subcribe(subPub);
        subcriber2.subcribe(subPub);

        publisher1.publish(subPub, "welcome", true);
        publisher1.publish(subPub, "yy", false);
        publisher1.publish(subPub, "232323", false);
        publisher1.publish(subPub, "to", true);
        subPub.update();

    }

}
