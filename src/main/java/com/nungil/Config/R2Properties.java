package com.nungil.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "r2")
public class R2Properties {
    private String endpoint;  // Cloudflare R2 엔드포인트 (https://<ACCOUNT_ID>.r2.cloudflarestorage.com)
    private String bucketName;
    private String accessKey;
    private String secretKey;
    private String publicUrl; // R2에 업로드된 파일을 접근하는 URL
}
