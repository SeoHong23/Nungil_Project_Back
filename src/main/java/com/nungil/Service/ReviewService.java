package com.nungil.Service;

import com.nungil.Dto.ReviewDTO;
import com.nungil.Repository.Interfaces.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;

    public void saveReview(ReviewDTO review) {
        reviewRepository.insertReview(review);
    }

    public List<ReviewDTO> getReviews (Long movieId, Long currentUserId) {
        return reviewRepository.getReviewsByMovieId(movieId, currentUserId);
    }
}
