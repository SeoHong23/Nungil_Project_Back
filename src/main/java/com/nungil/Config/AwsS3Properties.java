package com.nungil.Config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "aws.s3")
public class AwsS3Properties {
    private String region;
    private String bucketName;
    private String accessKey;
    private String secretKey;
}
