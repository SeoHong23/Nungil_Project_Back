package com.nungil.Document;

import com.nungil.Dto.StaffDTO;
import com.nungil.Dto.VideoDTO;
import com.nungil.Service.R2ImageService;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
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

    private String commCode; // 외부코드
    private String title; // 영화명
    private String titleEng; // 영문제명
    private String titleOrg; // 원제명
    private String titleEtc; // 기타제명(제명 검색을 위해 관리되는 제명 모음)
    private String prodYear; // 제작연도
    private String nation; // 국가
    private List<String> company; // 제작사
    private List<PlotDocument> plots; // 줄거리
    private String runtime; // 상영시간
    private String rating; // 심의등급
    private List<String> genre; // 장르
    private String type; // 유형구분
    private String use; // 용도구분
    private String releaseDate; // 대표 개봉일
    private List<String> posters; // 포스터
    private List<String> stlls; // 스틸이미지
    private List<StaffDocument> staffs; // 제작진(감독, 각본, 출연진, 스태프 순서)
    private String awards1;
    private String awards2;
    private List<String> keywords;
    private List<MovieDocument.OTTInfo> ottInfo;
    private Integer tmdbId;
    private boolean isCrawled = false;
    private Date lastCrawled;

    public void changeAllImgUrlHQ(R2ImageService r2ImageService) {
        posters = posters.stream()
                .map(poster -> imgUrlHQ("poster", poster, r2ImageService))
                .toList();
        stlls = stlls.stream()
                .map(still -> imgUrlHQ("still", still, r2ImageService))
                .toList();
    }

    public String imgUrlHQ(String type, String imgUrl, R2ImageService r2ImageService) {
        String newUrl = imgUrl;

        if (imgUrl.contains("/thm/01/")) {
            newUrl = imgUrl.replace("/thm/01/", "/" + type + "/");
        } else if (imgUrl.contains("/thm/02/")) {
            newUrl = imgUrl.replace("/thm/02/", "/" + type + "/");
        }
        newUrl = newUrl.replace("tn_", "");
        // 확장자 제거
        newUrl = newUrl.replaceAll("\\.[^.]+$", "");

        // "_숫자"가 있으면 그대로 두고, 없으면 "_01"을 추가
        if (newUrl.matches(".*_\\d+$")) {
            // "_숫자"가 있으면 그대로 둠
        } else {
            newUrl = newUrl + "_01";
        }
        try {
            return r2ImageService.processImage(newUrl,type); // S3로 이미지를 업로드하고 URL을 받음
        } catch (Exception e) {
            e.printStackTrace();
            return newUrl; // 오류 발생 시 원본 URL 반환
        }
    }

    public VideoDTO toDTO() {
        return VideoDTO.builder()
                .id(id)
                .title(title)
                .titleEng(titleEng)
                .nation(nation)
                .company(company)
                .prodYear(prodYear)
                .rating(rating)
                .genre(genre)
                .runtime(runtime)
                .releaseDate(formatDate(releaseDate))
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

    private String formatDate(String dateStr) {
        if (dateStr == null || dateStr.length() < 4) {
            return "미정";
        }

        if (dateStr.endsWith("00")) {
            dateStr = dateStr.substring(0, 6);
        }

        try {
            return switch (dateStr.length()) {
                case 4 -> dateStr + "년 개봉";
                case 6 -> dateStr.substring(0, 4) + "년 " + dateStr.substring(4, 6) + "월 개봉";
                case 8 -> dateStr.substring(0, 4) + "년 " + dateStr.substring(4, 6) + "월 " + dateStr.substring(6, 8) + "일 개봉";
                default -> "미정"; // 형식이 맞지 않으면 미정 반환
            };
        } catch (Exception e) {
            return "미정"; // 예외 발생 시 미정 반환
        }
    }

    private List<StaffDTO> getCast() {
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
                    .map(StaffDocument::getStaffNm)                           // 이름만 추출
                    .collect(Collectors.joining(", "));                        // 쉼표로 연결

            // 이름이 있을 경우에만 Map에 추가
            if (!names.isEmpty()) {
                directorsMap.put(role, names);
            }
        }

        return directorsMap;
    }

    private Map<String, String> getCrew() {
        // 제외할 역할 목록
        List<String> excludedRoles = List.of("감독", "각본", "각색", "제작자", "투자자", "투자사", "제작사", "배급사", "수입사", "출연");
        Map<String, String> crewMap = staffs.stream()
                .filter(staff -> !excludedRoles.contains(staff.getStaffRoleGroup()))  // 제외한 역할만 필터링
                .collect(Collectors.groupingBy(
                        StaffDocument::getStaffRoleGroup,
                        Collectors.mapping(StaffDocument::getStaffNm, Collectors.joining(", ")) // 쉼표로 연결
                ));

        return crewMap;
    }

    private Map<String, String> getMakers() {

        Map<String, String> makersMap = new HashMap<>();
        List<String> roles = List.of("제작자", "투자자", "투자사", "제작사", "배급사", "수입사");

        // 각 역할에 대해 반복 처리
        for (String role : roles) {
            // 해당 역할을 가진 staff 필터링 후 이름을 쉼표로 연결
            String names = staffs.stream()
                    .filter(staff -> role.equals(staff.getStaffRoleGroup()))  // 역할이 일치하는 staff만 필터링
                    .map(StaffDocument::getStaffNm)                     // 이름만 추출
                    .collect(Collectors.joining(", "));                // 쉼표로 연결

            if (!names.isEmpty()) {
                makersMap.put(role, names);
            }
        }

        return makersMap;
    }

}
