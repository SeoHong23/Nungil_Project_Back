package com.nungil.Repository;

import com.nungil.Document.VideoDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ApiVideoRepository extends MongoRepository<VideoDocument,String> {
    boolean existsByTitleAndProdYear(String title, String prodYear);

    List<VideoDocument> id(String id);
}
