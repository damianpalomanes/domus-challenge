package domus.challenge.service;

import reactor.core.publisher.Flux;

public interface DirectorService {
    Flux<String> getDirectorsByThreshold(int threshold);
}