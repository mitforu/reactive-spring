package com.example.demo.mongo;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.mongodb.repository.Tailable;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface TweetRepository extends ReactiveMongoRepository<Tweet, String> {

    @Tailable
    Flux<Tweet> findByCreatedBy(String s);
}