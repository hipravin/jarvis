package hipravin.jarvis.config;

import hipravin.jarvis.exception.NotFoundException;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.filter.ServerHttpObservationFilter;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ApiResponse(
            responseCode = "404",
            description = "Not found",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @ExceptionHandler(value = NotFoundException.class, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ProblemDetail> handleNotFound(NotFoundException ex, HttpServletRequest httpServletRequest) {
        registerErrorObservation(httpServletRequest, ex);//TODO: figure out why no errors in grafana

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setTitle("Not Found");

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        return ResponseEntity.of(problemDetail)
                .headers(headers).build();
    }

    @ApiResponse(
            responseCode = "400",
            description = "Bad request",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @ExceptionHandler(value = {IllegalArgumentException.class,
            ValidationException.class,
            MethodArgumentTypeMismatchException.class,
            MethodArgumentNotValidException.class}, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ProblemDetail> handleBadRequest(NotFoundException ex, HttpServletRequest httpServletRequest) {
        registerErrorObservation(httpServletRequest, ex);//TODO: figure out why no errors in grafana

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setTitle("Bad request");

        return ResponseEntity.of(problemDetail).build();
    }

    @ApiResponse(
            responseCode = "500",
            description = "Unexpected server error",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ProblemDetail.class)
            )
    )
    @ExceptionHandler(value = RuntimeException.class, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Object> handleUnhandled(RuntimeException ex, HttpServletRequest httpServletRequest) {
        registerErrorObservation(httpServletRequest, ex);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, null);
        problemDetail.setTitle("Internal Error");

        return ResponseEntity.of(problemDetail).build();
    }

    private void registerErrorObservation(HttpServletRequest httpServletRequest, Exception e) {
        ServerHttpObservationFilter.findObservationContext(httpServletRequest).ifPresent(context -> context.setError(e));
        log.error(e.getMessage(), e);
    }
}