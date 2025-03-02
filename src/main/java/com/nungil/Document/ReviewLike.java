package com.nungil.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "review_likes")
public class ReviewLike {
    @Id

    private String id;
    private int userId;
    private String reviewId;
    private LocalDateTime createdAt;
}
