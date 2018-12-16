package com.example.demo.mongo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

@RestController
public class TweetController {

    @Autowired
    private TweetRepository tweetRepository;

    @Autowired
    private ReactiveMongoTemplate reactiveMongoTemplate;

    @PostConstruct
    public void init(){
        reactiveMongoTemplate.dropCollection("tweets")
                .then(reactiveMongoTemplate.createCollection("tweets", CollectionOptions.empty().capped().size(2048).maxDocuments(10000))).subscribe();
    }

    @GetMapping("/add/tweet")
    public Mono<Tweet> saveTweet() {

        Tweet t = new Tweet();
        t.setText("#some random tweet");

        return tweetRepository.save(t);
    }


    @GetMapping("/tweets")
    public Flux<Tweet> getAllTweets() {
        return tweetRepository.findAll();
    }

    @PostMapping("/tweets")
    public Mono<Tweet> createTweets(@Valid @RequestBody Tweet tweet) {
        return tweetRepository.save(tweet);
    }

    @GetMapping("/tweets/{id}")
    public Mono<ResponseEntity<Tweet>> getTweetById(@PathVariable(value = "id") String tweetId) {
        return tweetRepository.findById(tweetId)
                .map(savedTweet -> ResponseEntity.ok(savedTweet))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping("/tweets/{id}")
    public Mono<ResponseEntity<Tweet>> updateTweet(@PathVariable(value = "id") String tweetId,
                                                   @Valid @RequestBody Tweet tweet) {
        return tweetRepository.findById(tweetId)
                .flatMap(existingTweet -> {
                    existingTweet.setText(tweet.getText());
                    return tweetRepository.save(existingTweet);
                })
                .map(updatedTweet -> new ResponseEntity<>(updatedTweet, HttpStatus.OK))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping("/tweets/{id}")
    public Mono<ResponseEntity<Void>> deleteTweet(@PathVariable(value = "id") String tweetId) {

        return tweetRepository.findById(tweetId)
                .flatMap(existingTweet ->
                        tweetRepository.delete(existingTweet)
                                .then(Mono.just(new ResponseEntity<Void>(HttpStatus.OK)))
                )
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Tweets are Sent to the client as Server Sent Events
    @CrossOrigin
    @GetMapping(value = "/stream/tweets", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Tweet> streamAllTweets() {
        return tweetRepository.findByCreatedBy("System");
    }


    // Tweets are Sent to the client as Server Sent Events
    @CrossOrigin
    @GetMapping(value = "/stream/cvs/tweets", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Tweet> streamAllTweetsWithCVS() {
        return tweetRepository.findByTextStartingWith("#CVS");
    }

    @CrossOrigin
    @GetMapping(value = "/stream/walgreens/tweets", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Tweet> streamAllTweetsWithWalgreens() {
        return tweetRepository.findByTextStartingWith("#Walgreens");
    }


    @CrossOrigin
    @GetMapping(value = "/stream/walgreens-and-cvs/tweets", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Tweet> streamAllTweetsWithWalgreensAndCVS() {
        return Flux.merge(
                tweetRepository.findByTextStartingWith("#CVS"),
                tweetRepository.findByTextStartingWith("#Walgreens")
        );
    }

    @CrossOrigin
    @GetMapping(value = "/stream-and-zip/walgreens-and-cvs/tweets", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<List<Tweet>> streamAllTweetsWithWalgreensAndCVSZip() {
        Flux<List<Tweet>> tweets = Flux.zip(
                tweetRepository.findByTextStartingWith("#CVS"),
                tweetRepository.findByTextStartingWith("#Walgreens"),
                (tweet, tweet2) -> Arrays.asList(tweet,tweet2)
        );
        return tweets;
    }
}