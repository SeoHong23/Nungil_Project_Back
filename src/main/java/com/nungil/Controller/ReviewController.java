package com.nungil.Controller;

import com.nungil.Document.ReviewDocument;
import com.nungil.Dto.ReviewDTO;
import com.nungil.Repository.Interfaces.ReviewRepository;
import com.nungil.Service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movie/reviews")
public class ReviewController {

    @Autowired
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/add")
    public ResponseEntity<String> createReview(@RequestBody ReviewDocument review) {
        reviewService.saveReview(review);
        return ResponseEntity.ok("리뷰가 등록되었습니다.");
    }

    @GetMapping("/{movieId}")
    public ResponseEntity<List<ReviewDocument>> getReviews(@PathVariable int movieId) {
        return ResponseEntity.ok(reviewService.getReviews(movieId));
    }


}
