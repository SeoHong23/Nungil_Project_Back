package com.nungil.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor

public class ReviewDTO {
    private int reviewId;
    private int userId;
    private String content;
    private double rating;
    private int movieId;


}
