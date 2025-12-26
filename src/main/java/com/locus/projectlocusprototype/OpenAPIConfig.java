package com.locus.projectlocusprototype;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Adds contact information and brief description for SwaggerAPI view

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Project Locus API")
                        .version("1.0")
                        .description("AI-powered spaced repetition system for automated flashcard generation")
                        .contact(new Contact()
                                .name("Mert Isik")
                                .email("mertisik329@gmail.com")
                                .url("https://github.com/packageIncoming")));
    }
}