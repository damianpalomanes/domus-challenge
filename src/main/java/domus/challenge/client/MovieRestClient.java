package domus.challenge.client;

import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface MovieRestClient {
    Mono<List<Map<String, Object>>> fetchMoviesByPage(int page);
}