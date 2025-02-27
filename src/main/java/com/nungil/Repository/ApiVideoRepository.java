package com.nungil.Repository;

import com.nungil.Document.VideoDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApiVideoRepository extends MongoRepository<VideoDocument, String> {
    boolean existsByTitleAndProdYear(String title, String prodYear);

    List<VideoDocument> id(String id);
}
