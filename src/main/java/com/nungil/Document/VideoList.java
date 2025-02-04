package com.nungil.Document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Data
@Document(collection = "video")  // MongoDB 컬렉션 이름
public class VideoList {
    @Id
    private String id;
    private String title;
    private String titleEng;
    private String titleOrg;
    private String prodYear;
    private String nation;
    private List<String> company;
    private List<String> genre;
    private String runtime;
    private String rating;
    private String type;
    private String use;
    private String releaseDate;
    private List<String> posters;
    private List<String> stlls;

    // 내부 객체 구조에 맞게 클래스 추가 (예: Plot)
    private List<Plot> plots;
    private List<Staff> staffs;
    private String awards1;
    private String awards2;
    private List<String> keywords;

    // 내부 클래스 정의
    @Data
    public static class Plot {
        private String plotLang;
        private String plotText;
    }

    @Data
    public static class Staff {
        private String staffNm;
        private String staffRoleGroup;
        private String staffRole;
        private String staffId;
    }
}
