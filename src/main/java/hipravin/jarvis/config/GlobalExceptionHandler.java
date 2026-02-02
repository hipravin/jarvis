package hipravin.jarvis.config;

import hipravin.jarvis.exception.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.filter.ServerHttpObservationFilter;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(value = {NotFoundException.class}, produces = {MediaType.APPLICATION_JSON_VALUE})
    protected ResponseEntity<ProblemDetail> handleNotFound(NotFoundException ex, HttpServletRequest httpServletRequest) {
        registerErrorObservation(httpServletRequest, ex);//TODO: figure out why no errors in grafana

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setTitle("Not Found");

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        return ResponseEntity.of(problemDetail)
                .headers(headers).build();
    }

    @ExceptionHandler(value = {RuntimeException.class}, produces = {MediaType.APPLICATION_JSON_VALUE})
    protected ResponseEntity<Object> handleUnhandled(RuntimeException ex, WebRequest request, HttpServletRequest httpServletRequest) {
        registerErrorObservation(httpServletRequest, ex);

        //never expose exception details to user like this, it's a serious security threat
//        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, null);
        problemDetail.setTitle("Internal Error");

        return handleExceptionInternal(ex,
                problemDetail,
                new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }



    private void registerErrorObservation(HttpServletRequest httpServletRequest, Exception e) {
        ServerHttpObservationFilter.findObservationContext(httpServletRequest).ifPresent(context -> context.setError(e));
        log.error(e.getMessage(), e);
    }
}