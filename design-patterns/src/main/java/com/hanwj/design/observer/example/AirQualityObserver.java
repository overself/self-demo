package com.hanwj.design.observer.example;

import com.hanwj.design.observer.Observer;

public class AirQualityObserver implements Observer<AirQuality> {

    private String cityCode;

    private AirQuality message;

    public AirQualityObserver(String cityCode){
        this.cityCode = cityCode;
    }

    @Override
    public String getTopic() {
        return cityCode;
    }

    @Override
    public void update(AirQuality value) {
        this.message = value;
        display();
    }

    public void display() {
        System.out.println("city:"+cityCode+", Value: " + message);
    }
}
