package com.locus.projectlocusprototype.AI;

import com.locus.projectlocusprototype.Flashcard.FlashcardResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
public class AIController {
    private final AIService aIService;

    public AIController(AIService aIService) {
        this.aIService = aIService;
    }

    @PostMapping()
    public ResponseEntity<List<FlashcardResponse>> generate(@RequestBody  AIRequest request,
                                                            Authentication authentication){
        List<FlashcardResponse> response = aIService.generateFlashcards(request,authentication);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
