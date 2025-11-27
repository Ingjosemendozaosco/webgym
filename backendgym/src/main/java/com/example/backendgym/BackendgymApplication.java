package com.example.backendgym;

import com.example.backendgym.config.EnvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.example.backendgym.repository")
@EntityScan(basePackages = "com.example.backendgym.domain")
@EnableScheduling
public class BackendgymApplication {

    public static void main(String[] args) {
        EnvLoader.load();
        
        SpringApplication.run(BackendgymApplication.class, args);
    }
}
