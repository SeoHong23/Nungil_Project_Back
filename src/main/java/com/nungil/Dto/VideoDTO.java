package com.nungil.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoDTO {
    private String id; // MongoDB의 ObjectId 필드, 자동으로 생성됨

    private String title; // 영화명
    private String titleEng; // 영문제명
    private String prodYear; // 제작연도
    private String nation; // 국가
    private List<String> company; // 제작사
    private String plots; // 줄거리
    private String runtime; // 상영시간
    private String rating; // 심의등급
    private List<String> genre; // 장르
    private String releaseDate; // 대표 개봉일
    private List<String> posters; // 포스터
    private List<String> stlls; // 스틸이미지
    private Map<String,String> directors; // 감독, 각본, 각색
    private List<StaffDTO> cast; // 출연
    private Map<String, String> makers; // 제작자 투자자 제작사 배급사 수입사
    private Map<String,String> crew; // 이외

    private String awards1;
    private String awards2;
    private List<String> keywords;

}
