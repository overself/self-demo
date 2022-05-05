package com.hanwj.design.observer.example;

import com.hanwj.design.observer.TopicMessage;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AirQuality implements TopicMessage {

    private String cityCode;

    private Integer level;

    private LocalDate localDate = LocalDate.now();

    @Override
    public String getTopic() {
        return cityCode;
    }
}
