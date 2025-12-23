package com.locus.projectlocusprototype.AI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;



@Configuration
public class AIConfig {
    @Value("${locus.ai.api-key}")
    private String API_KEY;
    @Bean
    public WebClient geminiWebClient(WebClient.Builder builder){
        // In order to send a proper request to the Gemini model, we need the following things:
        // 1. a generationConfig
        // 1b. systemInstructions (as part of the generationConfig, basically a system prompt)
        // 2.

        //THIS GENERATES A BARE-BONES WebClient THAT ONLY HAS THE CONTENT_TYPE AND API_KEY
        // WHEN CALLS ARE MADE TO THE GEMINI API, THAT IS WHEN THE REQUEST BODY WILL BE FILLED.
        return builder
                .baseUrl("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent?key="+API_KEY)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("X-goog-api-key",API_KEY)
                .build();
    }
}
