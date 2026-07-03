package com.greenharvest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // powers TokenBlacklistService's periodic cleanup
public class GreenHarvestApplication {

    public static void main(String[] args) {
        SpringApplication.run(GreenHarvestApplication.class, args);
    }
}
