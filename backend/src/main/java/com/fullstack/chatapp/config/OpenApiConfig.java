package com.fullstack.chatapp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI pulseChatOpenApi() {
        return new OpenAPI().info(new Info()
                .title("PulseChat API")
                .version("1.0")
                .description("REST and WebSocket API for the PulseChat real-time messaging application."));
    }
}
