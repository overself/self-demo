package com.hanwj.design.subpub;

/**
 * 消息类
 * @param <M>
 */
public class Msg<M> {

    private String publisher;

    private M message;

    public Msg(String publisher, M msg) {
        this.publisher = publisher;
        this.message = msg;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public M getMsg() {
        return message;
    }

    public void setMsg(M msg) {
        this.message = msg;
    }

}
