package domus.challenge.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import domus.challenge.service.DirectorService;
import reactor.core.publisher.Flux;

@RestController
public class DirectorControllerImpl {

    private final DirectorService directorService;

    public DirectorControllerImpl(DirectorService directorService) {
        this.directorService = directorService;
    }

    @GetMapping("/api/directors")
    public Flux<String> getDirectors(@RequestParam("threshold") int threshold) {
        if (threshold < 0) {
            throw new IllegalArgumentException("Threshold must be a non-negative integer.");
        }

        return directorService.getDirectorsByThreshold(threshold);
    }
}