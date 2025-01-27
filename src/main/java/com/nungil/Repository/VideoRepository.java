package com.nungil.Repository;

import com.nungil.Document.VideoDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface VideoRepository extends MongoRepository<VideoDocument,Long> {
}
