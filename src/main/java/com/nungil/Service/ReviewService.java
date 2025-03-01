package com.nungil.Service;

import com.nungil.Document.ReviewDocument;
import com.nungil.Dto.ReviewDTO;
import com.nungil.Repository.Interfaces.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;

    public void saveReview(ReviewDocument review) {
        review.setId(UUID.randomUUID().toString());
        review.setCreatedAt(LocalDateTime.now());

        List<ReviewDocument> existingReviews = reviewRepository.findByUserIdAndMovieId(
                review.getUserId(),
                review.getMovieId()
        );

        if(!existingReviews.isEmpty()) {
            throw new RuntimeException("이미 이 영화에 리뷰를 작성했습니다.");
        }
        reviewRepository.save(review);
    }

    public List<ReviewDocument> getReviews (int movieId) {
        return reviewRepository.findByMovieId(movieId);
    }
}
