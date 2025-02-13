package com.nungil.Repository.Interfaces;

import com.nungil.Document.MovieDocument;
import com.nungil.Dto.MovieDTO;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/*
    날짜 : 2025.01.23
    이름 : 박서홍
    내용 : MovieRepository 생성
 */
@Repository
public interface MovieRepository extends MongoRepository<MovieDocument, String> {
    // ✅ 대소문자 구분 없이 제목으로 영화 찾기
    Optional<MovieDocument> findByTitle(String title);

    // ✅ 크롤링하지 않은 영화 목록 찾기
    List<MovieDocument> findByIsCrawledFalse();

    // ✅ 직접 MongoDB 업데이트 쿼리 사용
    @Query("{ 'title' : ?0 }")
    @Update("{ '$set': { 'isCrawled' : ?1 } }")
    void updateCrawledStatus(String title, boolean isCrawled);
}
