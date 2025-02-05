package com.nungil.Repository;

import com.nungil.Document.VideoList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface VideoRepository extends MongoRepository<VideoList, String> {
    Optional<VideoList> findByTitle(String title);
    Page<VideoList> findAll(Pageable pageable);
    boolean existsById(String id);
}
