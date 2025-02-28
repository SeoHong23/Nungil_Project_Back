package com.nungil.Document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "rankingCache")
@Builder
@Data
public class RankingCacheDocument {
    @Id
    private String id;  // 캐시 키 (예: API URL)

    private Object data; // 캐싱할 데이터

    private String date;

    private String type;

    @Builder.Default
    private Instant createdAt = Instant.now(); // TTL 적용을 위한 생성 시간
}
