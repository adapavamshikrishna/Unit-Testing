package com.company.config;

import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Employee and Department Management API",
                version = "1.0.0",
                description = "REST API documentation for managing departments and employees. "
                        + "Includes CRUD operations, validation, pagination, sorting, search and filtering.",
                contact = @Contact(name = "API Support", email = "support@example.com"),
                license = @License(name = "Internal Project")
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Local development server")
        }
)
public class OpenApiConfig {
}
