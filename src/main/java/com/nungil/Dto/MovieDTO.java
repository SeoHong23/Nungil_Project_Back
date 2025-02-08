package com.nungil.Dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class OTTInfo {
        private String ottPlatform;
        private Boolean available;
        private String link;
    }
}
