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

import java.util.*;

@RestController
@RequestMapping("/api/movie/reviews")
public class ReviewController {

    @Autowired
    private final ReviewService reviewService;
    @Autowired
    private ReviewRepository reviewRepository;

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
    public ResponseEntity<List<ReviewDocument>> getReviews(
            @PathVariable String movieId,
            @RequestParam( required = false) Integer userId) {

        // 사용자 ID 가져오기 (실제로는 토큰에서 추출)

        List<ReviewDocument> reviews;
        if (userId != null && userId > 0) {
            reviews = reviewService.getReviewsWithLikeStatus(movieId, userId);
        } else {
            reviews = reviewService.getReviews(movieId);
        }
        Map<String, Object> response = new HashMap<>();
        response.put("count", reviews.size());
        response.put("reviews", reviews);

        return ResponseEntity.ok(reviews);
    }


    @PutMapping("/update")
    public ResponseEntity<String> updateReview(
//            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody ReviewDocument review) {
        try {
//            String token = authHeader != null && authHeader.startsWith("Bearer")
//                    ? authHeader.substring(7) : null;
            boolean update = reviewService.updateReview(review);
            if (update) {
                return ResponseEntity.ok("리뷰가 수정되었습니다.");
            } else {
                return ResponseEntity.badRequest().body("리뷰를 찾을 수 없거나 수정 권한이 없습니다.");
            }
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("리뷰 수정 중 오류가 발생했습니다.:" + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{reviewId}")
    public ResponseEntity<String> deleteReview(
            @PathVariable String reviewId,
            @RequestParam int userId) {
        try {
            boolean delete = reviewService.deleteReview(reviewId, userId);
            if (delete) {
                return ResponseEntity.ok("리뷰가 삭제 되었습니다.");
            } else {
                return ResponseEntity.badRequest().body("리뷰를 찾을 수 없거나 삭제 권한이 없습니다.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("리뷰 삭제 중 오류가 발생했습니다. :" +e.getMessage());
        }
    }

    @PostMapping("/like/{reviewId}")
    public ResponseEntity<String> toggleLike(
            @PathVariable String reviewId,
            @RequestBody Map<String, Object> requestBody ) {
        try {
            int userId = Integer.parseInt(requestBody.get("userId").toString());
            boolean liked = (boolean) requestBody.get("liked");

            boolean success = reviewService.toggleLike(reviewId, userId, liked);
            if (success) {
                return ResponseEntity.ok("좋아요 상태가 변경되었습니다.");
            } else {
                return ResponseEntity.ok("리뷰를 찾을 수 없거나 이미 원하시는 상태입니다.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("좋아요 처리 중 오류가 발생했습니다. :" +e.getMessage());
        }
    }


    @GetMapping("/count")
    public long getReviewCount(@RequestParam String movieId) {
        return reviewRepository.countByMovieId(movieId);
    }

}
