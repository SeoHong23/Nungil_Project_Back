package com.nungil.Repository.Interfaces;

import com.nungil.Document.VideoReaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReactionRepository extends MongoRepository<VideoReaction, String> {

}
