package com.nungil.Repository;

import com.nungil.Document.MovieDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
/*
    날짜 : 2025.01.23
    이름 : 박서홍
    내용 : MovieRepository 생성
 */

public interface MovieRepository extends MongoRepository<MovieDocument, String> {
}