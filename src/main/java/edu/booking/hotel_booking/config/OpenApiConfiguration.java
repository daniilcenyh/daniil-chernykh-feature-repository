package edu.booking.hotel_booking.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;


@Configuration
public class OpenApiConfiguration {
    @Value("${spring.application.name:Hotel Booking System}")
    private String appName;

    @Value("${spring.application.version:1.0.0}")
    private String appVersion;

    @Value("${spring.application.description:API для системы бронирования номеров в отеле}")
    private String appDescription;

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(appName)
                        .version(appVersion)
                        .description(appDescription))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development"),
                        new Server()
                                .url("https://api.hotelbooking.com")
                                .description("Production")
                ));
    }
}

