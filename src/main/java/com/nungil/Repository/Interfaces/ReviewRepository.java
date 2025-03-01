package com.nungil.Repository.Interfaces;

import com.nungil.Document.ReviewDocument;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ReviewRepository extends MongoRepository<ReviewDocument, String> {
    List<ReviewDocument> findByMovieId(int movieId);


    List<ReviewDocument> findByUserIdAndMovieId(Long userId, String movieId);
}
