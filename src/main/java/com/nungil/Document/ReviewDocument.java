package com.nungil.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "review")
public class ReviewDocument {
    @Id
    private String id;
    private Long userId;
    private int movieId;
    private String nick; // 작성자 닉네임
    private String content; // 리뷰 내용
    private double rating; // 평점
    private LocalDateTime createdAt;
    private int likeCount; // 좋아요 수
    private boolean isLiked; // 좋아요 눌렸는지 유무확인


}

