package com.nungil.Repository.Interfaces;

import com.nungil.Document.ReviewDocument;

import com.nungil.Document.ReviewLike;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends MongoRepository<ReviewDocument, String> {
    List<ReviewDocument> findByMovieId(String movieId);
    List<ReviewDocument> findByUserIdAndMovieId(Long userId, String movieId);

    long countByMovieId(String movieId);
}
