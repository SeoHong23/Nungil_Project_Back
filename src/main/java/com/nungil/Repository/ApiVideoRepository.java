package com.nungil.Repository;

import com.nungil.Document.VideoDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ApiVideoRepository extends MongoRepository<VideoDocument,Long> {
    boolean existsByTitleAndProdYear(String title, String prodYear);
}
