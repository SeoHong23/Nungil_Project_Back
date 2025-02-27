package com.nungil.Repository.Interfaces;

import com.nungil.Document.ReviewDocument;
import com.nungil.Dto.ReviewDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ReviewRepository extends MongoRepository<ReviewDocument, String> {
    List<ReviewDocument> findByMovieId(int movieId);



}
