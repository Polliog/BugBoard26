package it.unina.bugboard26;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Bugboard26Application {

    public static void main(String[] args) {
        SpringApplication.run(Bugboard26Application.class, args);
    }
}
