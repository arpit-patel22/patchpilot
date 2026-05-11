package ai.patchpilot.api.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Centralized exception handler for the REST API.
 *
 * <p>Ordering matters: more specific handlers are matched before less specific ones.
 * {@link ResponseStatusException} must be handled explicitly so controllers can throw
 * meaningful HTTP statuses (404, 409, etc.) without being swallowed by the generic
 * {@link Exception} catch-all at the bottom.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles validation failures from @Valid on request bodies. Returns a structured
     * 400 response with per-field error messages so the frontend can highlight specific
     * fields rather than showing a single generic error.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> fieldErrors.put(e.getField(), e.getDefaultMessage()));

        return ResponseEntity.badRequest().body(Map.of(
                "status", HttpStatus.BAD_REQUEST.value(),
                "error", "Validation failed",
                "fields", fieldErrors
        ));
    }

    /**
     * Handles failures from the Anthropic API or our wrapping logic — rate limits,
     * timeouts, malformed responses. Returns 502 Bad Gateway since the issue is
     * upstream of our service.
     */
    @ExceptionHandler(ClaudeApiException.class)
    public ResponseEntity<Map<String, Object>> handleClaudeApi(ClaudeApiException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of(
                "status", HttpStatus.BAD_GATEWAY.value(),
                "error", "Bad Gateway",
                "message", ex.getMessage()
        ));
    }

    /**
     * Handles ResponseStatusException thrown explicitly by controllers (e.g., 404 for
     * missing resources). Respects the status code on the exception instead of forcing
     * everything to 500.
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException ex) {
        HttpStatusCode status = ex.getStatusCode();
        return ResponseEntity.status(status).body(Map.of(
                "status", status.value(),
                "error", HttpStatus.valueOf(status.value()).getReasonPhrase(),
                "message", ex.getReason() != null ? ex.getReason() : "Request failed"
        ));
    }

    /**
     * Catch-all for unexpected runtime errors. Logs the actual exception server-side
     * (for debugging) but returns a generic message to the client (no stack trace leakage).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        log.error("Unhandled exception bubbled up to GlobalExceptionHandler", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "status", HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "error", "Internal Server Error",
                "message", "An unexpected error occurred"
        ));
    }
}