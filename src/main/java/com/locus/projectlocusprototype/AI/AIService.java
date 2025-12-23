package com.locus.projectlocusprototype.AI;

import com.locus.projectlocusprototype.Flashcard.Flashcard;
import com.locus.projectlocusprototype.Flashcard.FlashcardRequest;
import com.locus.projectlocusprototype.Flashcard.FlashcardResponse;
import com.locus.projectlocusprototype.Flashcard.FlashcardService;
import com.locus.projectlocusprototype.Note.Note;
import com.locus.projectlocusprototype.Note.NoteRequest;
import com.locus.projectlocusprototype.Note.NoteResponse;
import com.locus.projectlocusprototype.Note.NoteService;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
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
    private final NoteService noteService;
    private final FlashcardService flashcardService;
    private String systemPrompt;
    private final ObjectMapper objectMapper;


    public AIService(@Qualifier("geminiWebClient") WebClient geminiClient, ResourceLoader resourceLoader, NoteService noteService, ObjectMapper objectMapper, FlashcardService flashcardService) {
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
        this.noteService = noteService;
        this.flashcardService = flashcardService;
    }

    //  PRIVATE METHODS:

    //https://ai.google.dev/gemini-api/docs/text-generation#system-instructions
    // Generates the "system_instructions" object that is required for the Gemini API request
    // Includes the bare minimum, just the system prompt and structure
    private  HashMap<String, Object> generateSystemInstructionObject(){
        HashMap<String,Object> mainHashmap = new HashMap<String,Object>();
        ArrayList<Object> partsArray = new ArrayList<>();
        HashMap<String,String> textHashmap = new HashMap<>();

        textHashmap.put("text",this.systemPrompt);
        partsArray.add(textHashmap);
        mainHashmap.put("parts",partsArray);
        return mainHashmap;
    }

    // https://ai.google.dev/api/generate-content#generationconfig
    // Generates the generationConfig for the API request
    private HashMap<String,Object> generateGenerationConfigObject(){
        HashMap<String,Object> generationConfigMap = new HashMap<>();
        generationConfigMap.put("responseMimeType","application/json");
        return generationConfigMap;
    }


    // 'contents' is an Array of Objects, and each Object has a 'parts' array which has 'Part' objects,
    // which for this only need a 'text' property

    private ArrayList<Object> generateContentsObject(String promptText){
        ArrayList<Object> contentsObject = new ArrayList<>();

        HashMap<String,Object> contentObject = new HashMap<>();
        ArrayList<Object> partsArray = new ArrayList<>();
        HashMap<String,Object> part = new HashMap<>();

        part.put("text",promptText);
        partsArray.add(part);
        contentObject.put("parts",partsArray);
        contentsObject.add(contentObject);

        return contentsObject;
    }

    private GeminiRequest createGeminiRequest(AIRequest request){
        NoteResponse note = noteService.getNote(request.noteId(), request.userId());
        return new GeminiRequest(
                request.count(),
                note.title(),
                note.content()
        );
    }

    //  PUBLIC METHODS:

    public List<FlashcardResponse> generateFlashcards(AIRequest request) {
        // 1. Fetch context
        NoteResponse note = noteService.getNote(request.noteId(), request.userId());

        String promptText = String.format(
                "Generate %d flashcards for the topic '%s'.\n\nSOURCE CONTENT:\n%s",
                request.count(), note.title(), note.content()
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
            List<FlashcardDTO> extractedCards = objectMapper.readValue(jsonData, new TypeReference<List<FlashcardDTO>>() {});

            //System.out.println("DEBUG: Raw Response from Google: " + rawResponse);

            // 6. Save Flashcards and generate the list of FlashcardResponse objects
            List<FlashcardResponse> responses = new ArrayList<>();
            for (FlashcardDTO dto: extractedCards){
                responses.add(flashcardService.createFlashcard(new FlashcardRequest(
                        null,
                        request.userId(),
                        request.noteId(),
                        dto.front(),
                        dto.back()
                )));
            }


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
