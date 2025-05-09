package domus.challenge.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import domus.challenge.service.DirectorService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
public class DirectorControllerImpl implements DirectorController {

    private final DirectorService directorService;

    public DirectorControllerImpl(DirectorService directorService) {
        this.directorService = directorService;
    }

    @GetMapping("/api/directors")
    public Mono<Map<String, Object>> getDirectors(@RequestParam("threshold") int threshold) {
        if (threshold < 0) {
            throw new IllegalArgumentException("Threshold debe ser un valor positivo.");
        }

        return directorService.getDirectorsByThreshold(threshold)
                .collectList()
                .map(directors -> Map.of("directors", directors));
    }
}