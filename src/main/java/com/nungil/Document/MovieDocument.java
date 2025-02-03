package com.nungil.Document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/*
    날짜 : 2025.01.23
    이름 : 박서홍
    내용 :  MovieDocument 생성
 */

@Document(collection = "video")
public class MovieDocument {
    @Id
    private String id;       // MongoDB의 ObjectId
    private String title;    // 영화 제목
    private String titleEng; // 영어 제목
    private String nation;   // 제작 국가
    private String releaseDate; // 개봉일
    private List<String> genre; // 장르
    private String type;     // 영화 타입 (예: 극장용)
    private String runtime;  // 상영 시간
    private OTTInfo ottInfo; // OTT 정보 (추가 필드)

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitleEng() {
        return titleEng;
    }

    public void setTitleEng(String titleEng) {
        this.titleEng = titleEng;
    }

    public String getNation() {
        return nation;
    }

    public void setNation(String nation) {
        this.nation = nation;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public List<String> getGenre() {
        return genre;
    }

    public void setGenre(List<String> genre) {
        this.genre = genre;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRuntime() {
        return runtime;
    }

    public void setRuntime(String runtime) {
        this.runtime = runtime;
    }

    public OTTInfo getOttInfo() {
        return ottInfo;
    }

    public void setOttInfo(OTTInfo ottInfo) {
        this.ottInfo = ottInfo;
    }

    // *** OTTInfo 내부 클래스 (static으로 선언) ***
    public static class OTTInfo {
        private String platform; // OTT 플랫폼 이름 (예: Netflix, Disney+)
        private Boolean available; // 해당 OTT에서 사용 가능한지 여부
        private String link;         // 링크 정보 추가

        public String getPlatform() {
            return platform;
        }

        public void setPlatform(String platform) {
            this.platform = platform;
        }

        public Boolean getAvailable() {
            return available;
        }

        public void setAvailable(Boolean available) {
            this.available = available;
        }

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }

        @Override
        public String toString() {
            return "OTTInfo{" +
                    "platform='" + platform + '\'' +
                    ", available=" + available +
                    ", link='" + link + '\'' +
                    '}';
        }
    }
}
