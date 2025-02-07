package com.nungil.Repository;

import com.nungil.Document.VideoList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface VideoRepository extends MongoRepository<VideoList, String> {
    Optional<VideoList> findByTitle(String title);
    @Aggregation(pipeline = { "{$sample: {size: ?0}}" })
    List<VideoList> findRandom(int size);
    Page<VideoList> findAll(Pageable pageable);
    Page<VideoList> findByReleaseDateLessThanEqual(String today, Pageable pageable);

    boolean existsById(String id);

    List<VideoList> findByCommCodeIn(List<String> commCodes);
    List<VideoList> findByTitleIn(List<String> titles);
}
