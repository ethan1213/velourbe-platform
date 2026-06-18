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

    @Bean("userAuthClient")
    public WebClient userAuthClient() {
        return WebClient.builder().baseUrl(userAuthUrl).build();
    }

    @Bean("rentalClient")
    public WebClient rentalClient() {
        return WebClient.builder().baseUrl(rentalUrl).build();
    }
}
