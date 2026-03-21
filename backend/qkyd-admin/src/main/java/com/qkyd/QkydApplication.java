package com.qkyd;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class QkydApplication {
    public static void main(String[] args) {
        SpringApplication.run(QkydApplication.class, args);
    }
}
