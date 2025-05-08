package domus.challenge.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import domus.challenge.client.MovieRestClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
        return Flux.range(1, 100)
                .concatMap(page -> {
                    List<Map<String, Object>> cachedMovies = cache.getIfPresent(page);
                    if (cachedMovies != null) {
                        log.info("Datos obtenidos del caché para la página {}", page);
                        return Mono.just(cachedMovies);
                    }
                    return movieRestClient.fetchMoviesByPage(page)
                            .doOnNext(movies -> {
                                if (movies != null && !movies.isEmpty()) {
                                    cache.put(page, movies);
                                    log.info("Datos almacenados en caché para la página {}", page);
                                }
                            });
                })
                .takeWhile(movies -> movies != null && !movies.isEmpty())
                .flatMap(Flux::fromIterable)
                .map(movie -> (String) movie.get("Director"))
                .filter(Objects::nonNull)
                .groupBy(director -> director)
                .flatMap(group -> group.count()
                        .map(count -> Map.entry(group.key(), count))
                        .onErrorResume(e -> {
                            log.error("Error al contar películas para el director: " + group.key(), e);
                            return Mono.empty();
                        }))
                .filter(entry -> entry.getValue() > threshold)
                .map(entry -> entry.getKey() + " - " + entry.getValue())
                .onErrorResume(e -> {
                    log.error("Error inesperado en el flujo reactivo", e);
                    return Flux.empty();
                });
    }
}