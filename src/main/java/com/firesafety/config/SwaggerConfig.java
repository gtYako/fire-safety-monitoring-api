package com.firesafety.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Fire Safety Monitoring API",
        version = "1.0.0",
        description = "REST API for fire safety monitoring system of educational buildings. " +
                      "Manages buildings, rooms, sensors, readings, alerts, and incidents.",
        contact = @Contact(name = "Fire Safety System", email = "admin@firesafety.local")
    )
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "Enter JWT token obtained from POST /api/auth/login"
)
public class SwaggerConfig {
    // Конфигурация только описывает OpenAPI/Swagger, отдельная логика здесь не нужна.
}
