package com.locus.projectlocusprototype.Flashcard;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/flashcards")
public class FlashcardController {
    private final FlashcardService flashcardService;

    public FlashcardController(FlashcardService flashcardService) {
        this.flashcardService = flashcardService;
    }

    //  GET mapping to get a single flashcard
    @GetMapping("/{flashcardId}")
    public ResponseEntity<FlashcardResponse> one(@PathVariable Long flashcardId,
                                                 Authentication authentication) {
        FlashcardResponse response = flashcardService.getFlashcard(flashcardId,authentication);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    //  GET mapping to get all flashcards for a single note
    @GetMapping("/bynote/{noteId}")
    public ResponseEntity<Page<FlashcardResponse>> allForNote(@RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "20") int size,
                                                              @PathVariable Long noteId,
                                                              Authentication authentication){
        Page<FlashcardResponse> response = flashcardService.getFlashcardsForNote(noteId,page,size,authentication);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    //  GET mapping to get all flashcards for a user
    @GetMapping("/byuser")
    public ResponseEntity<Page<FlashcardResponse>> allForUser(@RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "20") int size,
                                              Authentication authentication){
        Page<FlashcardResponse> response = flashcardService.getFlashcardsForUser(page,size,authentication);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // POST mapping to make a single flashcard for a single note
    @PostMapping("/create")
    public ResponseEntity<FlashcardResponse> createFlashcard(@Valid @RequestBody FlashcardRequest request,
                                                             Authentication authentication){
        FlashcardResponse response = flashcardService.createFlashcard(request,authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // DELETE mapping to delete a single flashcard by ID
    @DeleteMapping("/delete/{flashcardId}")
    public ResponseEntity<String> deleteFlashcard(@PathVariable Long flashcardId,
                                                  Authentication authentication){
        flashcardService.deleteFlashcard(flashcardId,authentication);
        return ResponseEntity.status(HttpStatus.OK).body("Successfully deleted flashcard with id " + flashcardId );
    }

    //  PUT mapping to update either the front or back of a flashcard
    @PutMapping("/change/{flashcardId}")
    public ResponseEntity<FlashcardResponse> updateFlashcard(
            @PathVariable Long flashcardId,
            @Valid @RequestBody FlashcardRequest request,
            Authentication authentication){
        FlashcardResponse response = flashcardService.updateFlashcard(flashcardId,request,authentication);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // USE A ReviewRequest DTO AS THE INPUT IN THE BODY WHICH TRIGGERS
    // AN SM-2 CALCULATION THROUGH THE SpacedRepetitionService
    @PatchMapping("/review/{flashcardId}")
    public ResponseEntity<FlashcardResponse> reviewNote(@PathVariable Long flashcardId,
                                                        @Valid @RequestBody ReviewRequest reviewContent,
                                                        Authentication authentication
                                                        ){
        FlashcardResponse response = flashcardService.reviewFlashcard(flashcardId,reviewContent,authentication);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
