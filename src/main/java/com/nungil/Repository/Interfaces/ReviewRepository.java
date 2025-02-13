package com.nungil.Repository.Interfaces;

import com.nungil.Dto.ReviewDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReviewRepository {
    void insertReview(ReviewDTO review);

    List<ReviewDTO> findReviewsByMovie(@Param("movieId") int movieId);
}
