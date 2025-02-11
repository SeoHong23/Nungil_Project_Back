package com.nungil.Service;

import com.nungil.Document.RankingCacheDocument;
import com.nungil.Repository.CacheRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class CacheService {

    @Autowired
    private CacheRepository cacheRepository;

    public Optional<RankingCacheDocument> getFromCache(String date, String type) {
        return cacheRepository.findByDateAndType(date, type);
    }

    public void saveToCache(Object data, String date, String type) {
        RankingCacheDocument cacheEntity = RankingCacheDocument.builder()
                .data(data)
                .date(date)
                .type(type)
                .build();
        cacheRepository.save(cacheEntity);
    }
}