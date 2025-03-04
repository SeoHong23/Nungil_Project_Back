package com.nungil.Service;

import com.nungil.Document.ReviewDocument;
import com.nungil.Document.ReviewLike;
import com.nungil.Repository.Interfaces.ReviewLikeRepository;
import com.nungil.Repository.Interfaces.ReviewRepository;
import com.nungil.Repository.Interfaces.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final UserRepository userRepository;

    public void saveReview(ReviewDocument review) {
        if (review.getNick() != null) {
            review.setNick(processNickname(review.getNick()));
        }

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


    public List<ReviewDocument> getReviews(String movieId) {
        List<ReviewDocument> reviews = reviewRepository.findByMovieId(movieId);
        for (ReviewDocument review : reviews) {
            if (review.getNick() != null) {
                review.setNick(processNickname(review.getNick()));
            }
        }
        System.out.println("🔍 검색된 리뷰 개수: " + reviews.size());
        return reviews;
    }

    private String processNickname(String nickname) {
        if (nickname == null) {
            return null;
        }

        try {
            // ISO-8859-1로 해석된 UTF-8 문자열을 다시 UTF-8로 변환
            byte[] bytes = nickname.getBytes("ISO-8859-1");
            String decodedNickname = new String(bytes, StandardCharsets.UTF_8);

            System.out.println("원본 닉네임: " + nickname);
            System.out.println("처리된 닉네임: " + decodedNickname);

            return decodedNickname;
        } catch (Exception e) {
            System.out.println("닉네임 처리 오류: " + e.getMessage());
            return nickname;
        }
    }

    public List<ReviewDocument> getReviewsWithLikeStatus(String movieId, int userId) {
        List<ReviewDocument> reviews = reviewRepository.findByMovieId(movieId);

        for (ReviewDocument review : reviews) {
            boolean isLiked = reviewLikeRepository.existsByUserIdAndReviewId(userId, review.getId());
            review.setLiked(isLiked);

            if (review.getNick() != null) {
                review.setNick(processNickname(review.getNick()));
            }
        }

        System.out.println("🔍 검색된 리뷰 개수: " + reviews.size() + " (좋아요 상태 포함)");
        return reviews;
    }



    public boolean updateReview(ReviewDocument review) {
        Optional<ReviewDocument> existingReview = reviewRepository.findById(review.getId());

        if(existingReview.isPresent()) {
            ReviewDocument reviewDocument = existingReview.get();

            if(!existingReview.get().getUserId().equals(review.getUserId()) ) {
                System.out.println("리뷰 수정 권한 없음: 요청 사용자 ID(" + review.getUserId() +")와 " +
                        "리뷰 작성자 ID(" + existingReview.get().getUserId() + ")가 일치 하지 않습니다.");
                return false;
            }

            reviewDocument.setContent(review.getContent());
            reviewDocument.setRating(review.getRating());
            reviewDocument.setCreatedAt(LocalDateTime.now());

            reviewRepository.save(reviewDocument);
            System.out.println("리뷰 수정 성공 : ID =" + reviewDocument.getId());
            return true;
        } else {
            System.out.println("리뷰 수정 실패 : ID = " + review.getId() + "님의 리뷰를 찾을 수 없습니다.");
            return false;
        }
    }

    public boolean deleteReview(String reviewId, int userId) {
        Optional<ReviewDocument> existingReviewOpt = reviewRepository.findById(reviewId);

        if (existingReviewOpt.isPresent()) {
            ReviewDocument existingReview = existingReviewOpt.get();

            if(existingReview.getUserId() != userId) {
                System.out.println("리뷰 삭제 권한 없음: 요청 사용자 ID("+ userId + ")와 리뷰 작성자 ID(" + existingReview.getUserId() +")가 일치하지 않습니다");
                return false;
            }
            reviewRepository.deleteById(reviewId);
            System.out.println("리뷰 삭제 성공: ID = " + reviewId);
            return true;
        } else {
            System.out.println("리뷰 삭제 실패: ID = " +reviewId + "리뷰를 찾을 수 없습니다.");
            return false;
        }
    }

    public boolean toggleLike(String reviewId, int userId, boolean liked) {
        Optional<ReviewDocument> existingReviewOpt =reviewRepository.findById(reviewId);

        if (existingReviewOpt.isPresent()) {
            ReviewDocument review =existingReviewOpt.get();

            boolean hasLiked = reviewLikeRepository.existsByUserIdAndReviewId(userId,reviewId);

            if(liked && !hasLiked) {
                ReviewLike newLike = new ReviewLike();
                newLike.setId(UUID.randomUUID().toString());
                newLike.setUserId(userId);
                newLike.setReviewId(reviewId);
                newLike.setCreatedAt(LocalDateTime.now());
                reviewLikeRepository.save(newLike);

                int likeCount = reviewLikeRepository.countByReviewId(reviewId);
                review.setLikeCount(likeCount);
                reviewRepository.save(review);

                System.out.println("✅ 좋아요 추가 성공: 리뷰 ID = " + reviewId + ", 사용자 ID = " + userId + ", 총 좋아요 수 = " + likeCount);
                return true;
            } else if(!liked && hasLiked) {
                reviewLikeRepository.deleteByUserIdAndReviewId(userId, reviewId);

                int likeCount = reviewLikeRepository.countByReviewId(reviewId);
                review.setLikeCount(likeCount);
                reviewRepository.save(review);
                System.out.println("✅ 좋아요 제거 성공: 리뷰 ID = " + reviewId + ", 사용자 ID = " + userId + ", 총 좋아요 수 = " + likeCount);
                return true;
            }
            System.out.println("좋아요 상태 변경 실패 : 이미 원하시는 상태입니다.");
            return false;
        }
        System.out.println("⚠️ 좋아요 토글 실패: 리뷰 ID = " + reviewId + " 리뷰를 찾을 수 없음");
        return false;
    }
}
