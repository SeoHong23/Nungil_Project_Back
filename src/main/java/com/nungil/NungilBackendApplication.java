package com.nungil;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableMongoRepositories
@SpringBootApplication(scanBasePackages = {"com.nungil", "config"}) // config 패키지 스캔
@EnableScheduling  // 스케줄링 기능 활성화
public class NungilBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(NungilBackendApplication.class, args);
    }

}
