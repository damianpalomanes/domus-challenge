package domus.challenge.service;

            import domus.challenge.client.MovieRestClient;
            import org.springframework.stereotype.Service;
            import reactor.core.publisher.Flux;

            import java.util.Map;

@Service
            public class DirectorServiceImpl implements DirectorService {

                private final MovieRestClient movieRestClient;

                public DirectorServiceImpl(MovieRestClient movieRestClient) {
                    this.movieRestClient = movieRestClient;
                }

                @Override
                public Flux<String> getDirectorsByThreshold(int threshold) {
                    return Flux.range(1, Integer.MAX_VALUE)
                            .concatMap(page -> movieRestClient.fetchMoviesByPage(page))
                            .takeWhile(movies -> movies != null && !movies.isEmpty())
                            .flatMap(Flux::fromIterable)
                            .map(movie -> (String) movie.get("Director"))
                            .filter(director -> director != null)
                            .groupBy(director -> director)
                            .flatMap(group -> group.count().map(count -> Map.entry(group.key(), count)))
                            .filter(entry -> entry.getValue() > threshold)
                            .map(entry -> entry.getKey() + " - " + entry.getValue());
                }
            }