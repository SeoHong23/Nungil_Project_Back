package com.nungil.Dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieDTO {
    private String title;    // 영화 제목
    private String titleEng; // 영어 제목
    private String nation;   // 제작 국가
    private String releaseDate; // 개봉일
    private List<String> genre; // 장르
    private String type;     // 영화 타입
    private String runtime;  // 상영 시간
    private List<OTTInfo> ottInfo; // OTT 정보
    private boolean isInTheater;  // 🔥 추가: 영화관 상영 여부
    private List<String> theaterLinks; // 영화관 이름 및 링크

    public List<OTTInfo> getOttInfo() {
        return ottInfo.stream().map(OTTInfo::transLink).collect(Collectors.toList());
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class OTTInfo {
        private String ottPlatform;
        private Boolean available;
        private String link;

        public OTTInfo transLink(){
            return OTTInfo.builder()
                    .ottPlatform(ottPlatform)
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
