package com.locus.projectlocusprototype.AI;

import com.locus.projectlocusprototype.Note.Note;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

// The functionality of the AIService is the following
//  1. Construct the request body going to the Gemini API
//  2. Return the contents as a DTO to other services using AIService

@Service
public class AIService {
    private final WebClient geminiClient; // Provides the connection and minimum requirements (content_type and api_key)


    public AIService(@Qualifier("geminiWebClient") WebClient geminiClient) {
        this.geminiClient = geminiClient;
    }

//    public AIInternalResponse makeNoteRequest(Note note){
//
//
//    }


}
