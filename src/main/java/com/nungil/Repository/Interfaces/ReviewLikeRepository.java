package com.nungil.Repository.Interfaces;

import com.nungil.Document.ReviewDocument;
import com.nungil.Document.ReviewLike;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewLikeRepository extends MongoRepository<ReviewLike, String> {
    Optional<ReviewLike> findByUserIdAndReviewId(int userId, String reviewId);
    boolean existsByUserIdAndReviewId(int userId, String reviewId);
    void deleteByUserIdAndReviewId(int userId, String reviewId);
    int countByReviewId(String reviewId);
    List<ReviewLike> findByUserId(int userId);
}
