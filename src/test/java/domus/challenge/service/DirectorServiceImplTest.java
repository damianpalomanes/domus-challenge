package domus.challenge.service;

import domus.challenge.client.MovieRestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;


class DirectorServiceImplTest {

    private MovieRestClient movieRestClient;
    private DirectorServiceImpl directorService;

    @BeforeEach
    void setUp() {
        movieRestClient = mock(MovieRestClient.class);
        directorService = new DirectorServiceImpl(movieRestClient);
    }

    @Test
    void testGetDirectorsByThreshold_WithValidData() {
        List<Map<String, Object>> page1Movies = List.of(
                Map.of("Director", "Director A"),
                Map.of("Director", "Director B"),
                Map.of("Director", "Director A")
        );

        List<Map<String, Object>> page2Movies = List.of(
                Map.of("Director", "Director B"),
                Map.of("Director", "Director C"),
                Map.of("Director", "Director A")
        );

        when(movieRestClient.fetchMoviesByPage(1)).thenReturn(Mono.just(page1Movies));
        when(movieRestClient.fetchMoviesByPage(2)).thenReturn(Mono.just(page2Movies));
        when(movieRestClient.fetchMoviesByPage(3)).thenReturn(Mono.just(List.of()));

        Flux<String> result = directorService.getDirectorsByThreshold(0);

        StepVerifier.create(result)
                .expectNext("Director A - 3", "Director B - 2", "Director C - 1")
                .verifyComplete();

        verify(movieRestClient, times(1)).fetchMoviesByPage(1);
        verify(movieRestClient, times(1)).fetchMoviesByPage(2);
        verify(movieRestClient, times(1)).fetchMoviesByPage(3);
    }

@Test
void testGetDirectorsByThreshold_WithEmptyData() {
    when(movieRestClient.fetchMoviesByPage(1)).thenReturn(Mono.just(List.of()));

    Flux<String> result = directorService.getDirectorsByThreshold(1);

    StepVerifier.create(result)
            .expectErrorMatches(throwable -> throwable instanceof ResponseStatusException &&
                    ((ResponseStatusException) throwable).getStatusCode().equals(HttpStatus.NOT_FOUND) &&
                    throwable.getMessage().contains("No se encontraron películas."))
            .verify();

    verify(movieRestClient, times(1)).fetchMoviesByPage(1);
}

    @Test
    void testGetDirectorsByThreshold_WithError() {
        when(movieRestClient.fetchMoviesByPage(1)).thenReturn(Mono.error(new RuntimeException("API error")));

        Flux<String> result = directorService.getDirectorsByThreshold(1);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof ResponseStatusException &&
                        ((ResponseStatusException) throwable).getStatusCode().equals(HttpStatus.SERVICE_UNAVAILABLE) &&
                        throwable.getMessage().contains("Error al obtener datos de películas."))
                .verify();

        verify(movieRestClient, times(1)).fetchMoviesByPage(1);
    }
}