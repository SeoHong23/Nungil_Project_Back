package com.nungil.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.List;
import java.util.Map;

/*
    날짜 : 2025.01.23
    이름 : 박서홍
    내용 : MovieDocument 생성 (Lombok 사용)
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "video")
public class MovieDocument {
    @Id
    private String id;                  // MongoDB의 ObjectId
    private String title;               // 영화 제목
    private String titleEng;            // 영어 제목
    private String nation;              // 제작 국가
    private String releaseDate;         // 개봉일
    private List<String> genre;         // 장르
    private String type;                // 영화 타입 (예: 극장용)
    private String runtime;             // 상영 시간
    private List<OTTInfo> ottInfo;      // OTT 정보

    @Field("isCrawled")
    private boolean isCrawled = false;  // 기본값: 크롤링되지 않음

    @Field("lastCrawled")
    private Date lastCrawled; // 마지막 크롤링 시간

    @Field("inTheater")
    private boolean inTheater;

    @Field("theaterLinks")  // ✅ 영화관 예매 정보 저장 필드 추가
    private List<Map<String, String>> theaterLinks;


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OTTInfo {
        private String platform;        // OTT 플랫폼 이름 (예: Netflix, Disney+)
        private Boolean available;      // 해당 OTT에서 사용 가능한지 여부
        private String link;            // 링크 정보
    }
}
