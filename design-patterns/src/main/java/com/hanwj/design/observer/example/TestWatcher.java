package com.hanwj.design.observer.example;

public class TestWatcher {

    public static void main(String[] args) {
        AirQualitySubject airSubject = new AirQualitySubject();
        airSubject.attach(new AirQualityObserver("1001"));
        airSubject.attach(new AirQualityObserver("1002"));

        AirQuality quality = new AirQuality();
        quality.setCityCode("1001");
        quality.setLevel(1);
        airSubject.putMessage(quality);

        quality = new AirQuality();
        quality.setCityCode("1002");
        quality.setLevel(2);
        airSubject.putMessage(quality);

        quality = new AirQuality();
        quality.setCityCode("1003");
        quality.setLevel(3);
        airSubject.putMessage(quality);
    }

}
