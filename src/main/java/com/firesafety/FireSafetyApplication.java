package com.firesafety;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class FireSafetyApplication {

    // Точка входа в Spring Boot приложение.
    // Здесь поднимается веб-сервер, подключается база данных и загружаются все Bean-компоненты.
    public static void main(String[] args) {
        SpringApplication.run(FireSafetyApplication.class, args);
    }
}
