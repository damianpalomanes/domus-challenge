package domus.challenge.controller;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface DirectorController {
    Mono<Map<String, Object>> getDirectors(int threshold);
}