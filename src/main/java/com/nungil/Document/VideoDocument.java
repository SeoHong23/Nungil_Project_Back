package com.nungil.Document;

import com.nungil.Service.S3ImageService;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;


@Builder
@Data
@Document(collection = "video")
public class VideoDocument {

    @Id
    private String id; // MongoDB의 ObjectId 필드, 자동으로 생성됨

    @Field("commCode")
    private String commCode; // 외부코드

    @Field("title")
    private String title; // 영화명

    @Field("titleEng")
    private String titleEng; // 영문제명

    @Field("titleOrg")
    private String titleOrg; // 원제명

    @Field("titleEtc")
    private String titleEtc; // 기타제명(제명 검색을 위해 관리되는 제명 모음)

    @Field("prodYear")
    private String prodYear; // 제작연도

    @Field("nation")
    private String nation; // 국가

    @Field("company")
    private List<String> company; // 제작사

    @Field("plots")
    private List<PlotDocument> plots; // 줄거리

    @Field("runtime")
    private String runtime; // 상영시간

    @Field("rating")
    private String rating; // 심의등급

    @Field("genre")
    private List<String> genre; // 장르

    @Field("type")
    private String type; // 유형구분

    @Field("use")
    private String use; // 용도구분

    @Field("releaseDate")
    private String releaseDate; // 대표 개봉일

    @Field("posters")
    private List<String> posters; // 포스터

    @Field("stlls")
    private List<String> stlls; // 스틸이미지

    @Field("staffs")
    private List<StaffDocument> staffs; // 제작진(감독, 각본, 출연진, 스태프 순서)

    @Field("awards1")
    private String awards1;

    @Field("awards2")
    private String awards2;

    @Field("keywords")
    private List<String> keywords;

    public void changeAllImgUrlHQ(S3ImageService s3ImageService) {
        posters = posters.stream()
                .map(poster -> imgUrlHQ("poster", poster, s3ImageService))
                .toList();
        stlls = stlls.stream()
                .map(still -> imgUrlHQ("still", still, s3ImageService))
                .toList();
    }

    public String imgUrlHQ(String type, String imgUrl, S3ImageService s3ImageService) {
        String newUrl = imgUrl;
        // "/thm/01/"이 포함되어 있으면 그것을 type으로 변경
        if (imgUrl.contains("/thm/01/")) {
            newUrl = imgUrl.replace("/thm/01/", "/" + type + "/");
        }
        // "/thm/02/"가 포함되어 있으면 그것을 type으로 변경
        else if (imgUrl.contains("/thm/02/")) {
            newUrl = imgUrl.replace("/thm/02/", "/" + type + "/");
        }
        newUrl = newUrl.replace("tn_", "").replace(".jpg", "_01"); // 2. "tn_"을 제거하고 "_01"을 추가

        // 여기에 s3ImageService를 통해 S3 업로드 로직을 처리할 수 있도록 추가
        try {
            return s3ImageService.processImage(newUrl); // S3로 이미지를 업로드하고 URL을 받음
        } catch (Exception e) {
            e.printStackTrace();
            return newUrl; // 오류 발생 시 원본 URL 반환
        }
    }

}
