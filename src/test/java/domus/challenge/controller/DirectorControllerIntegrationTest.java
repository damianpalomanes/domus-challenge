package domus.challenge.controller;

import domus.challenge.service.DirectorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@WebFluxTest(DirectorControllerImpl.class)
@Import(DirectorControllerIntegrationTest.TestConfig.class)
class DirectorControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private DirectorService directorService;

    @Test
    void testGetDirectors_WithValidThreshold() {
        when(directorService.getDirectorsByThreshold(5))
                .thenReturn(Flux.just("Director 1 - 10", "Director 2 - 8"));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/directors")
                        .queryParam("threshold", "5")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.directors").isArray()
                .jsonPath("$.directors[0]").isEqualTo("Director 1 - 10")
                .jsonPath("$.directors[1]").isEqualTo("Director 2 - 8");
    }

    @Test
    void testGetDirectors_WithNegativeThreshold() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/directors")
                        .queryParam("threshold", "-1")
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        @Primary
        public DirectorService directorService() {
            return mock(DirectorService.class);
        }
    }
}