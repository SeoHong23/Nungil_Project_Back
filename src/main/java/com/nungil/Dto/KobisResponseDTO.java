package com.nungil.Dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class KobisResponseDTO {

    @JsonProperty("boxOfficeResult")
    private BoxOfficeResult boxOfficeResult;

    public List<MovieInfo> getDailyBoxOfficeList() {
        return boxOfficeResult != null ? boxOfficeResult.getDailyBoxOfficeList() : null;
    }

    public List<MovieInfo> getWeeklyBoxOfficeList() {
        return boxOfficeResult != null ? boxOfficeResult.getWeeklyBoxOfficeList() : null;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BoxOfficeResult {

        @JsonProperty("boxofficeType")
        private String boxofficeType;  // "일별 박스오피스" 또는 "주간 박스오피스"

        @JsonProperty("showRange")
        private String showRange;  // 예: "20250127~20250202"

        @JsonProperty("yearWeekTime")
        private String yearWeekTime; // 주간 박스오피스만 존재

        @JsonProperty("dailyBoxOfficeList")
        private List<MovieInfo> dailyBoxOfficeList;

        @JsonProperty("weeklyBoxOfficeList")
        private List<MovieInfo> weeklyBoxOfficeList;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MovieInfo {
        @JsonProperty("movieCd")
        private String movieCd;

        @JsonProperty("movieNm")
        private String movieNm;

        @JsonProperty("rank")
        private String rank;

        @JsonProperty("rankInten")
        private String rankInten;

        @JsonProperty("rankOldAndNew")
        private String rankOldAndNew;
    }
}
