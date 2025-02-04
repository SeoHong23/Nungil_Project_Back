package com.nungil.Repository.Interfaces;

import com.nungil.Document.MovieDocument;
import com.nungil.Dto.MovieDTO;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/*
    날짜 : 2025.01.23
    이름 : 박서홍
    내용 : MovieRepository 생성
 */
@Repository
public interface MovieRepository extends MongoRepository<MovieDocument, String> {
    Optional<MovieDocument> findByTitle(String title);

    @Query("{ 'title': { $regex: ?0, $options: 'i' } }") // 대소문자 무시
    List<MovieDocument> findByTitleRegex(String title);
}
