package cl.velourbe.bff.proxy;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.net.URI;

/**
 * Generic passthrough of the BFF: forwards every /api/** request that is not
 * an aggregated /api/bff endpoint to the microservice that owns the path,
 * preserving method, query string, Authorization header, and body.
 */
@Slf4j
@RestController
public class ProxyController {

    private final ProxyRoutes routes;
    private final WebClient proxyClient;

    public ProxyController(ProxyRoutes routes,
                           @Qualifier("proxyClient") WebClient proxyClient) {
        this.routes = routes;
        this.proxyClient = proxyClient;
    }

    @RequestMapping({
            "/api/auth/**", "/api/users/**",
            "/api/scooters/**", "/api/rentals/**",
            "/api/payments/**", "/api/notifications/**",
            "/api/analytics/**", "/api/logistics/**",
            "/api/maintenance/**", "/api/support/**",
            "/api/stations/**", "/api/reviews/**",
            "/api/auth", "/api/users",
            "/api/scooters", "/api/rentals",
            "/api/payments", "/api/notifications",
            "/api/analytics", "/api/logistics",
            "/api/maintenance", "/api/support",
            "/api/stations", "/api/reviews"
    })
    public ResponseEntity<byte[]> proxy(HttpServletRequest request,
                                        @RequestBody(required = false) byte[] body) {
        String path = request.getRequestURI();
        String baseUrl = routes.resolve(path);
        if (baseUrl == null) {
            log.warn("Sin ruta para {}", path);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        String query = request.getQueryString();
        URI target = URI.create(baseUrl + path + (query != null ? "?" + query : ""));
        HttpMethod method = HttpMethod.valueOf(request.getMethod());
        log.info("Proxy {} {} -> {}", method, path, target);

        try {
            WebClient.RequestBodySpec spec = proxyClient.method(method)
                    .uri(target)
                    .headers(h -> copyHeaders(request, h));
            return (body != null && body.length > 0
                    ? spec.bodyValue(body).retrieve()
                    : spec.retrieve())
                    .toEntity(byte[].class)
                    .block();
        } catch (WebClientResponseException ex) {
            log.warn("Upstream {} respondió {}: {}", target, ex.getStatusCode(), ex.getMessage());
            return ResponseEntity.status(ex.getStatusCode())
                    .contentType(ex.getHeaders().getContentType() != null
                            ? ex.getHeaders().getContentType() : MediaType.APPLICATION_JSON)
                    .body(ex.getResponseBodyAsByteArray());
        } catch (WebClientRequestException ex) {
            log.error("Microservicio no disponible para {}: {}", path, ex.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"status\":503,\"message\":\"Servicio no disponible\"}".getBytes());
        }
    }

    private void copyHeaders(HttpServletRequest request, HttpHeaders headers) {
        String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (auth != null) {
            headers.set(HttpHeaders.AUTHORIZATION, auth);
        }
        String contentType = request.getContentType();
        if (contentType != null) {
            headers.set(HttpHeaders.CONTENT_TYPE, contentType);
        }
        String accept = request.getHeader(HttpHeaders.ACCEPT);
        if (accept != null) {
            headers.set(HttpHeaders.ACCEPT, accept);
        }
    }
}
