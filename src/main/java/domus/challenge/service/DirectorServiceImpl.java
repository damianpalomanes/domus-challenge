package domus.challenge.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import domus.challenge.client.MovieRestClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.core.publisher.Mono;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class DirectorServiceImpl implements DirectorService {

    private final MovieRestClient movieRestClient;
    private final Cache<Integer, List<Map<String, Object>>> cache;

    public DirectorServiceImpl(MovieRestClient movieRestClient) {
        this.movieRestClient = movieRestClient;
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .maximumSize(100)
                .build();
    }

    @Override
    public Flux<String> getDirectorsByThreshold(int threshold) {
        return fetchAllMovies()
                .flatMap(Flux::fromIterable)
                .map(this::extractDirector)
                .filter(Objects::nonNull)
                .groupBy(director -> director)
                .flatMap(this::countMoviesByDirector)
                .filter(entry -> entry.getValue() > threshold)
                .sort((entry1, entry2) -> Long.compare(entry2.getValue(), entry1.getValue()))
                .map(this::formatDirectorCount)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontraron directores que cumplan con el umbral.")))
                .onErrorMap(this::mapToAppropriateException);
    }

    private Flux<List<Map<String, Object>>> fetchAllMovies() {
        return Flux.range(1, 100)
                .concatMap(this::fetchMoviesByPage)
                .takeWhile(movies -> movies != null && !movies.isEmpty())
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontraron películas.")));
    }

    private Mono<List<Map<String, Object>>> fetchMoviesByPage(int page) {
        List<Map<String, Object>> cachedMovies = cache.getIfPresent(page);
        if (cachedMovies != null) {
            log.info("Datos obtenidos del caché para la página {}", page);
            return Mono.just(cachedMovies);
        }
        return movieRestClient.fetchMoviesByPage(page)
                .doOnNext(movies -> cacheMovies(page, movies))
                .onErrorMap(e -> {
                    log.error("Error al obtener películas para la página {}", page, e);
                    return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Error al obtener datos de películas.");
                });
    }

    private void cacheMovies(int page, List<Map<String, Object>> movies) {
        if (movies != null && !movies.isEmpty()) {
            cache.put(page, movies);
            log.info("Datos almacenados en caché para la página {}", page);
        }
    }

    private String extractDirector(Map<String, Object> movie) {
        return (String) movie.get("Director");
    }

    private Mono<Map.Entry<String, Long>> countMoviesByDirector(GroupedFlux<String, String> group) {
        return group.count()
                .map(count -> Map.entry(group.key(), count))
                .onErrorMap(e -> {
                    log.error("Error al contar películas para el director: {}", group.key(), e);
                    return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al procesar datos del director.");
                });
    }

    private String formatDirectorCount(Map.Entry<String, Long> entry) {
        return entry.getKey() + " - " + entry.getValue();
    }

    private Throwable mapToAppropriateException(Throwable e) {
        log.error("Error inesperado en el flujo reactivo", e);
        if (e instanceof ResponseStatusException) {
            return e;
        }
        return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error inesperado en el servicio.");
    }
}