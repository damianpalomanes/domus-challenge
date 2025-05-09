# Domus Challenge

## Descripción
Domus Challenge es una aplicación desarrollada en **Java** utilizando **Spring Boot** y **WebFlux**. Su propósito es gestionar directores y proporcionar una API para obtener información basada en un umbral específico.

## Tecnologías utilizadas
- **Java 17**
- **Spring Boot 3.4.1**
- **Spring WebFlux**
- **SpringDoc OpenAPI**
- **Maven**

## Endpoints principales
### Obtener directores por umbral
- **URL:** `/api/directors`
- **Método:** `GET`
- **Parámetros:**
  - `threshold` (int): Umbral para filtrar directores. Debe ser un valor positivo.
- **Descripción:** Devuelve una lista de directores cuyo umbral cumple con el valor especificado.

## Posibles mensajes de retorno
### Respuestas exitosas
- **Código 200:** 
  ```json
  {
    "directors": [
      {
        "id": 1,
        "name": "Director Name",
        "threshold": 10
      }
    ]
  }
  
### Errores
- **Código 400:** Umbral negativo.
  ```json
  {
    "error": "Threshold debe ser un valor positivo."
  }

- **Código 500:** Error interno del servidor.
  ```json
  {
    "error": "Ocurrió un error inesperado: [detalle del error]"
  }
  
## Manejo de excepciones
El manejo de errores se realiza mediante la clase `GlobalExceptionHandler`, que captura las siguientes excepciones:

- **IllegalArgumentException:** Devuelve un código 400 con el mensaje de error.
- **ResponseStatusException:** Devuelve el código de estado y la razón especificada.
- **Exception:** Devuelve un código 500 con un mensaje genérico.

## Documentación Swagger
La documentación de la API está disponible en el siguiente enlace:

[Swagger UI](http://localhost:8080/swagger-ui.html)

> **Nota:** Asegúrate de que la aplicación esté en ejecución para acceder a la documentación.

## Ejecución del proyecto
1. Clona el repositorio.
2. Asegúrate de tener **Java 17** y **Maven** instalados.
3. Ejecuta el siguiente comando para compilar y ejecutar la aplicación:
   ```bash
   mvn spring-boot:run