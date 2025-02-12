package com.nungil.Document;

import com.nungil.Dto.MovieDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

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

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OTTInfo {
        private String platform;        // OTT 플랫폼 이름 (예: Netflix, Disney+)
        private Boolean available;      // 해당 OTT에서 사용 가능한지 여부
        private String link;            // 링크 정보

        public OTTInfo transLink(){
            return OTTInfo.builder()
                    .platform(platform)
                    .available(available)
                    .link(transformUrl(link))
                    .build();
        }

        private static String transformUrl(String inputUrl) {
            try {
                // 원본 URL에서 "url=" 이후 값을 추출
                String encodedUrl = inputUrl.split("url=")[1].split("&")[0];

                // URL 디코딩
                String decodedUrl = URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8);

                // 새로운 파라미터 추가
                if (decodedUrl.contains("?")) {
                    return decodedUrl + "&source=nungil"; // 이미 ?가 있으면 &로 이어 붙임
                } else {
                    return decodedUrl + "?source=nungil"; // ?가 없으면 ?로 시작
                }
            } catch (Exception e) {
                e.printStackTrace();
                return inputUrl;
            }
        }
    }
}
