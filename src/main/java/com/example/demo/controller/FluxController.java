package com.example.demo.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Random;

@RestController
public class FluxController {

    @GetMapping("/number")
    private Mono<Integer> getRandomNumber() {
        return Mono.just(new Random().nextInt());
    }

    @GetMapping(value = "/numbers", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    private Flux<Integer> getRandomNumbers() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(val -> new Random().nextInt());
    }
}
