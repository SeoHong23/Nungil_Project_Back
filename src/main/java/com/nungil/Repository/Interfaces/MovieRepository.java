package com.nungil.Repository.Interfaces;

import com.nungil.Document.MovieDocument;
import com.nungil.Dto.MovieDTO;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
/*
    날짜 : 2025.01.23
    이름 : 박서홍
    내용 : MovieRepository 생성
 */
@Repository
public interface MovieRepository extends MongoRepository<MovieDocument, String> {

    @Update("{ '$set': { 'ottLinks': ?1 } }")
    @Query("{'title' :  ?0 }")
    void updateOttInfoByTitle(String title, List<MovieDocument.OTTInfo> ottInfoList);
}