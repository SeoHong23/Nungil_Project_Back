package com.nungil.config;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class MinioConfig {

    private final R2Properties r2Properties;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(r2Properties.getEndpoint()) // Cloudflare R2 엔드포인트
                .credentials(r2Properties.getAccessKey(), r2Properties.getSecretKey())
                .build();
    }
}
