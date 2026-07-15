package cl.velourbe.bff.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Global error handler for the BFF.
 * Centralizes the translation of upstream microservice failures into a
 * uniform BffErrorResponse so the frontend always sees a single, stable
 * error contract regardless of which upstream service failed.
 */
@Slf4j
@RestControllerAdvice
public class GlobalBffExceptionHandler {

    /**
     * Handles failures from any upstream microservice (via WebClient).
     * Passes through the upstream's HTTP status and tries to surface its
     * "message" field verbatim in the BFF error body.
     */
    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<BffErrorResponse> handleUpstream(WebClientResponseException ex) {
        HttpStatus upstream = HttpStatus.valueOf(ex.getStatusCode().value());
        log.warn("Upstream error from {}: status={} body={}",
                ex.getRequest() != null ? ex.getRequest().getURI() : "n/a",
                upstream, ex.getResponseBodyAsString());

        String message = upstream.getReasonPhrase();
        try {
            Map<String, Object> body = new ObjectMapper()
                    .readValue(ex.getResponseBodyAsString(), Map.class);
            Object msg = body.get("message");
            if (msg != null) {
                message = msg.toString();
            }
        } catch (Exception parseFail) {
            // upstream body wasn't JSON or didn't have a "message" field; keep the status phrase
        }

        return ResponseEntity.status(upstream)
                .body(new BffErrorResponse(
                        upstream.value(),
                        "UPSTREAM_ERROR",
                        message,
                        LocalDateTime.now()));
    }

    /**
     * Catch-all for anything else bubbling up to the BFF.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BffErrorResponse> handleGeneric(Exception ex) {
        log.error("Unexpected error in BFF", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new BffErrorResponse(
                        500,
                        "INTERNAL_ERROR",
                        ex.getMessage() != null ? ex.getMessage() : "Internal server error",
                        LocalDateTime.now()));
    }
}
