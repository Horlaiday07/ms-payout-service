package com.remit.mellonsecure.payout.config;

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
    public OpenAPI payoutOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Payout API")
                        .description("Production-grade fintech payout platform - Name enquiry, transfers, batch transfers, transaction query")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Support")
                                .email("support@example.com")))
                .servers(List.of(
                        new Server().url("/").description("Default Server")
                ));
    }
}
