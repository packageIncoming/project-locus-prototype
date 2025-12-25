package com.locus.projectlocusprototype.AI;

import com.locus.projectlocusprototype.Auth.AuthService;
import com.locus.projectlocusprototype.Auth.User;
import com.locus.projectlocusprototype.Exceptions.InvalidAIRequestException;
import com.locus.projectlocusprototype.Flashcard.*;
import com.locus.projectlocusprototype.Note.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// The functionality of the AIService is the following
//  1. Construct the request body going to the Gemini API
//  2. Return the contents to other  services using AIService

// The Gemini API expects 3 main objects in the request body:
//  1. A generationConfig object
//  2. A system_instruction object
//  3. A contents object



@Service
public class AIService {
    private final WebClient geminiClient; // Provides the connection and minimum requirements (content_type and api_key)
    private final AuthService authService;
    private final NoteRepository noteRepository;
    private final FlashcardRepository flashcardRepository;
    private final FlashcardService flashcardService;
    private String systemPrompt;
    private final ObjectMapper objectMapper;


    public AIService(@Qualifier("geminiWebClient") WebClient geminiClient, ResourceLoader resourceLoader, ObjectMapper objectMapper, AuthService authService, NoteRepository noteRepository, FlashcardRepository flashcardRepository, FlashcardService flashcardService) {
        // initialize gemini client:
        this.geminiClient = geminiClient;
        this.objectMapper = objectMapper;

        // initialize system prompt:
        Resource systemPromptFile = resourceLoader.getResource("classpath:static/system-prompt.txt");
        try {
            this.systemPrompt = systemPromptFile.getContentAsString(Charset.defaultCharset());
        } catch (IOException e) {
            this.systemPrompt = "ERROR: System prompt was not found. If reading this, return 'ERROR'";
            throw new RuntimeException(e);
        }
        this.authService = authService;
        this.noteRepository = noteRepository;
        this.flashcardRepository = flashcardRepository;
        this.flashcardService = flashcardService;
    }


    //  PUBLIC METHODS:

    public List<FlashcardResponse> generateFlashcards(AIRequest request, Authentication authentication) {
        //  Get user
        User user = authService.getUserFromAuthenticationObject(authentication);

        //  Get note
        Note note = noteRepository.findNoteById(request.noteId()).orElseThrow(
                ()-> new InvalidAIRequestException("ERROR: Note with ID" + request.noteId() + " does not exist")
        );

        // Check if this user owns this note
        if (!note.getUser().equals(user)){
            throw new InvalidAIRequestException("ERROR: Note with ID" + request.noteId() + " does not exist");
        }


        String promptText = String.format(
                "Generate %d flashcards for the topic '%s'.\n\nSOURCE CONTENT:\n%s",
                request.count(), note.getTitle(), note.getContent()
        );
        // 2. Build the request structure
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", promptText)
                        ))
                ),
                "systemInstruction",Map.of(
                        "parts", Map.of(
                                "text",systemPrompt
                        )
                ),
                "generationConfig", Map.of(
                        "responseMimeType","application/json"//,
                )

        );

        try {
            //System.out.println("DEBUG: Sending request to Gemini...");
            //System.out.println("DEBUG: Request Body: " + objectMapper.writeValueAsString(requestBody));

            // 3. Convert to GeminiResponse object to make parsing easier
            GeminiResponse geminiResponse = geminiClient.post()
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(GeminiResponse.class)
                    .block();

            //4. Get the actual JSON text from the response
            assert geminiResponse != null;
            String jsonData = geminiResponse.extractText();

            // 5. Convert to FlashcardDTO objects
            List<FlashcardDTO> extractedCards = objectMapper.readValue(jsonData, new TypeReference<>() {
            });

            //System.out.println("DEBUG: Raw Response from Google: " + rawResponse);

            // 6. Save Flashcards and generate the list of FlashcardResponse objects
            List<FlashcardResponse> responses = new ArrayList<>();
            for (FlashcardDTO dto: extractedCards){
                Flashcard flashcard = new Flashcard( note, user, dto.back(), dto.front());
                flashcardRepository.save(flashcard);
                //  Add the flashcard to its associated note's hashset
                note.getFlashcards().add(flashcard);
                responses.add(flashcardService.flashcardToResponse(flashcard));
            }
            //  Update flashcard collection
            noteRepository.save(note);


            // Return empty list for now just to stop the 500 error while we debug logs
            return responses;
        } catch (WebClientResponseException e) {
            System.err.println("DEBUG: Google API Error: " + e.getStatusCode());
            System.err.println("DEBUG: Error Body: " + e.getResponseBodyAsString());
            throw new RuntimeException("AI Error: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("DEBUG: General Error: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }


    // Internal DTO for parsing the AI's JSON output
    public record FlashcardDTO(String front, String back) {}
}
