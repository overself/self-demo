package com.hanwj.design.observer.example;

import com.hanwj.design.observer.Observer;
import com.hanwj.design.observer.Subject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AirQualitySubject extends Subject<AirQuality> {

    @Override
    public void notifyObservers(AirQuality message) {
        for (Observer observer : observers) {
            if (observer.getTopic().equals(message.getTopic())) {
                observer.update(message);
            } else {
                log.info("{}未订阅：{}", observer.getTopic(),message);
            }
        }
    }
}
