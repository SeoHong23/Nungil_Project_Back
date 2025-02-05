package com.nungil.Repository;

import com.nungil.Document.VideoDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ApiVideoRepository extends MongoRepository<VideoDocument,Long> {
    boolean existsByCommCode(String commCode); // commCode가 존재하는지 확인하는 메서드

    int findAllCommCodes();
}
