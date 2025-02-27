package com.nungil.Repository.Interfaces;

import com.nungil.Dto.ReviewDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

@Mapper
public interface ReviewRepository extends MongoRepository<ReviewDTO, String> {
    void insertReview(ReviewDTO review);

    List<ReviewDTO> findReviewsByMovie(@Param("movieId") int movieId);
    List<ReviewDTO> getReviewsByMovieId(@Param("movieId") Long movieId, @Param("currentUserId") Long currentUserId);
    List<ReviewDTO> findByMovieId(Long movieId);

}
