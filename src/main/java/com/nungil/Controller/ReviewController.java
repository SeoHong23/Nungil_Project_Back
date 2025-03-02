package com.nungil.Controller;

import com.nungil.Document.ReviewDocument;
import com.nungil.Dto.ReviewDTO;
import com.nungil.Repository.Interfaces.ReviewRepository;
import com.nungil.Service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<String> createReview(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody ReviewDocument review) {
        try {
            // 인증 체크 (필요한 경우)
             String token = authHeader != null && authHeader.startsWith("Bearer ")
                 ? authHeader.substring(7) : null;

             reviewService.saveReview(review);
             return ResponseEntity.ok("리뷰가 등록되었습니다.");
        }  catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("리뷰 등록 중 오류가 발생했습니다.");
        }
    }

    @GetMapping("/{movieId}")
    public ResponseEntity<List<ReviewDocument>> getReviews(@PathVariable String movieId) {
        return ResponseEntity.ok(reviewService.getReviews(movieId));
    }


}
