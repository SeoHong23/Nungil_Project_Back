package com.nungil.Document;

import com.nungil.Dto.StaffDTO;
import com.nungil.Dto.VideoDTO;
import com.nungil.Service.S3ImageService;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


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

        if (imgUrl.contains("/thm/01/")) { newUrl = imgUrl.replace("/thm/01/", "/" + type + "/");}
        else if (imgUrl.contains("/thm/02/")) {newUrl = imgUrl.replace("/thm/02/", "/" + type + "/");}
        newUrl = newUrl.replace("tn_", "").replaceAll("\\.[^.]+$", "_01");

        try {
            return s3ImageService.processImage(newUrl); // S3로 이미지를 업로드하고 URL을 받음
        } catch (Exception e) {
            e.printStackTrace();
            return newUrl; // 오류 발생 시 원본 URL 반환
        }
    }

    public VideoDTO toDTO(){
        return VideoDTO.builder()
                .id(id)
                .title(title)
                .titleEng(titleEng)
                .nation(nation)
                .company(company)
                .prodYear(prodYear)
                .rating(rating)
                .genre(genre)
                .releaseDate(releaseDate)
                .posters(posters)
                .stlls(stlls)
                .awards1(awards1)
                .awards2(awards2)
                .keywords(keywords)
                .plots(plots.get(0).getPlotText())
                .cast(this.getCast())
                .directors(this.getDirectors())
                .makers(this.getMakers())
                .crew(this.getCrew())
                .build();
    }

    private List<StaffDTO> getCast(){
        List<StaffDocument> castList = staffs.stream().filter(staffDocument -> staffDocument.getStaffRoleGroup().equals("출연")).toList();
        return castList.stream().map(StaffDocument::toDTO).collect(Collectors.toList());
    }
    private Map<String, String> getDirectors() {
        // 최종 결과를 담을 Map
        Map<String, String> directorsMap = new HashMap<>();

        // 필터링할 역할 목록
        List<String> roles = List.of("감독", "각본", "각색");

        // 각 역할에 대해 반복 처리
        for (String role : roles) {
            // 해당 역할을 가진 staff 필터링 후 이름을 쉼표로 연결
            String names = staffs.stream()
                    .filter(staff -> role.equals(staff.getStaffRoleGroup()))  // 역할이 일치하는 staff만 필터링
                    .map(StaffDocument::getStaffNm)                     // 이름만 추출
                    .collect(Collectors.joining(", "));                // 쉼표로 연결

            directorsMap.put(role, names);
        }

        return directorsMap;
    }
    private Map<String, String> getCrew() {
        // 제외할 역할 목록
        List<String> excludedRoles  = List.of("감독", "각본", "각색", "제작자", "투자자", "투자사", "제작사", "배급사", "수입사", "출연");
        Map<String, String> crewMap =  staffs.stream()
                    .filter(staff -> !excludedRoles.contains(staff.getStaffRoleGroup()))  // 제외한 역할만 필터링
                    .collect(Collectors.groupingBy(
                            StaffDocument::getStaffRoleGroup,
                            Collectors.mapping(StaffDocument::getStaffNm,Collectors.joining(", ")) // 쉼표로 연결
                    ));

        return crewMap;
    }

    private String getMakers(){
        List<String> targetRoles = List.of("제작자", "투자자", "투자사", "제작사", "배급사", "수입사");
        // 역할별로 이름을 수집하고 "이름(역할)" 형식으로 변환
        String result = staffs.stream()
                .filter(staff -> targetRoles.contains(staff.getStaffRoleGroup()))  // 관심 있는 역할만 필터링
                .map(staff -> staff.getStaffNm() + "(" + staff.getStaffRoleGroup() + ")") // "이름(역할)" 형식으로 변환
                .collect(Collectors.joining(", "));  // 쉼표로 연결

        return result;
    }

    private String makers; // 제작자 투자자 제작사 배급사 수입사


}
