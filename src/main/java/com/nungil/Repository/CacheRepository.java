package com.nungil.Repository;

import com.nungil.Document.RankingCacheDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CacheRepository extends MongoRepository<RankingCacheDocument, String> {
    Optional<RankingCacheDocument> findByDateAndType(String date, String type);
}
