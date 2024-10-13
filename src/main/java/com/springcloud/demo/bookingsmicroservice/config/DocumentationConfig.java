package com.springcloud.demo.bookingsmicroservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                title = "Bookings microservice",
                description = "Microservice to handle app bookings",
                version = "1.0.0",
                contact = @Contact(
                        name = "Gonzalo Jerez",
                        email = "gonzalojerezn@gmail.com",
                        url = "github.com/GonzaJerez"
                )
        ),
        servers = {
                @Server(
                        url = "http://localhost:8080",
                        description = "Dev server"
                )
        }
)
public class DocumentationConfig {
}
