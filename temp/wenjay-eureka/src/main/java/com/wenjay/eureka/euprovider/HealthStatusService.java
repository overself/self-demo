package com.wenjay.eureka.euprovider;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.stereotype.Service;

@Service
public class HealthStatusService implements HealthIndicator {
    boolean status = true;

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getStatus() {
        if(this.status)  {
            return Status.UP.getCode();
        } else {
            return Status.DOWN.getCode();
        }
    }

    @Override
    public Health health() {
        if (status) {
            return new Health.Builder().up().build();
        }
        return new Health.Builder().down().build();

    }
}
