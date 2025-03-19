package com.nungil.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor

public class ReviewDTO {
    private String reviewId;
    private Long userId;
    private String movieId;
    private String movieTitle;
    private String nick; // 작성자 닉네임
    private String content; // 리뷰 내용
    private double rating; // 평점
    private LocalDateTime createdAt;
    private int likeCount; // 좋아요 수
    private boolean isLiked; // 좋아요 눌렸는지 유무확인



}
