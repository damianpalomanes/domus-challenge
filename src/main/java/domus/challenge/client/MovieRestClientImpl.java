package domus.challenge.client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
public class MovieRestClientImpl implements MovieRestClient {

    private final WebClient webClient;

    public MovieRestClientImpl(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://challenge.iugolabs.com/api/movies").build();
    }

    @Override
    public Mono<List<Map<String, Object>>> fetchMoviesByPage(int page) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/search")
                        .queryParam("page", page)
                        .build())
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> (List<Map<String, Object>>) response.get("data"))
                .onErrorResume(WebClientResponseException.class, e -> {
                    return Mono.error(new RuntimeException("Error al consumir la API externa: " + e.getMessage(), e));
                });
    }
}