package com.nungil;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@EnableMongoRepositories
@SpringBootApplication(scanBasePackages = {"com.nungil", "config"}) // config 패키지 스캔
public class NungilBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(NungilBackendApplication.class, args);
    }

}
