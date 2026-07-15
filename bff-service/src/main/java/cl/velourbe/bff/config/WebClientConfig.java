package cl.velourbe.bff.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${services.user-auth.url}")
    private String userAuthUrl;

    @Value("${services.rental.url}")
    private String rentalUrl;

    @Value("${services.support.url}")
    private String supportUrl;

    @Value("${services.maintenance.url}")
    private String maintenanceUrl;

    @Bean("userAuthClient")
    public WebClient userAuthClient() {
        return WebClient.builder().baseUrl(userAuthUrl).build();
    }

    @Bean("rentalClient")
    public WebClient rentalClient() {
        return WebClient.builder().baseUrl(rentalUrl).build();
    }

    @Bean("supportClient")
    public WebClient supportClient() {
        return WebClient.builder().baseUrl(supportUrl).build();
    }

    @Bean("maintenanceClient")
    public WebClient maintenanceClient() {
        return WebClient.builder().baseUrl(maintenanceUrl).build();
    }

    /**
     * WebClient sin baseUrl usado por el ProxyController para reenviar
     * peticiones a cualquier microservicio según la tabla de rutas.
     */
    @Bean("proxyClient")
    public WebClient proxyClient() {
        return WebClient.builder().build();
    }
}
