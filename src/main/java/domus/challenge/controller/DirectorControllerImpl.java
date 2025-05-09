package domus.challenge.controller;

                import io.swagger.v3.oas.annotations.Operation;
                import io.swagger.v3.oas.annotations.media.Content;
                import io.swagger.v3.oas.annotations.media.Schema;
                import io.swagger.v3.oas.annotations.responses.ApiResponse;
                import io.swagger.v3.oas.annotations.responses.ApiResponses;
                import io.swagger.v3.oas.annotations.tags.Tag;
                import org.springframework.web.bind.annotation.GetMapping;
                import org.springframework.web.bind.annotation.RequestParam;
                import org.springframework.web.bind.annotation.RestController;
                import domus.challenge.service.DirectorService;
                import reactor.core.publisher.Mono;

                import java.util.Map;

                @RestController
                @Tag(name = "Directors", description = "API para gestionar directores")
                public class DirectorControllerImpl implements DirectorController {

                    private final DirectorService directorService;

                    public DirectorControllerImpl(DirectorService directorService) {
                        this.directorService = directorService;
                    }

                    @Operation(summary = "Obtener directores por umbral", description = "Devuelve una lista de directores cuyo umbral cumple con el valor especificado.")
                    @ApiResponses(value = {
                            @ApiResponse(responseCode = "200", description = "Directores obtenidos exitosamente",
                                    content = @Content(mediaType = "application/json",
                                            schema = @Schema(implementation = Map.class))),
                            @ApiResponse(responseCode = "400", description = "Solicitud inválida (umbral negativo)",
                                    content = @Content(mediaType = "application/json")),
                            @ApiResponse(responseCode = "500", description = "Error interno del servidor",
                                    content = @Content(mediaType = "application/json"))
                    })
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