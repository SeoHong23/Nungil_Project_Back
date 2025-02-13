package com.nungil.Controller;

import com.nungil.Dto.ReviewDTO;
import com.nungil.Repository.Interfaces.ReviewRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movie/reviews")
public class ReviewController {

    private ReviewRepository reviewRepository;

    public ReviewController(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @PostMapping("/add")
    public String createReview(@RequestBody ReviewDTO reviewDTO) {
        reviewRepository.insertReview(reviewDTO);
        return "리뷰 저장완료";
    }

    @GetMapping("/{movieId}")
    public List<ReviewDTO> getReviews(@PathVariable int movieId) {
        return reviewRepository.findReviewsByMovie(movieId);
    }


}
