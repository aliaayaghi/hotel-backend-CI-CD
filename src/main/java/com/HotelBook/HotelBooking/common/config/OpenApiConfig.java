package com.HotelBook.HotelBooking.common.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI hotelBookOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("HotelBook API")
                        .description("Hotel booking system — SWER313 Course Project")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("HotelBook Team")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local development server")
                ));
    }
}