package com.example.audioindexer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class AudioIndexerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AudioIndexerApplication.class, args);
    }
}