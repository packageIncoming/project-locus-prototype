package com.locus.projectlocusprototype.Flashcard;

import com.locus.projectlocusprototype.Note.NoteRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
                                                 @Valid @RequestBody FlashcardRequest request) {
        FlashcardResponse response = flashcardService.getFlashcard(flashcardId,request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    //  GET mapping to get all flashcards for a single note
    @GetMapping("/bynote/{noteId}")
    public ResponseEntity<List<FlashcardResponse>> allForNote(@PathVariable Long noteId, @RequestBody NoteRequest request){
        List<FlashcardResponse> response = flashcardService.getFlashcardsForNote(noteId,request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    //  GET mapping to get all flashcards for a user
    @GetMapping("/byuser/{userId}")
    public ResponseEntity<List<FlashcardResponse>> allForUser(@PathVariable Long userId){
        List<FlashcardResponse> response = flashcardService.getFlashcardsForUser(userId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // POST mapping to make a single flashcard for a single note
    @PostMapping("/create")
    public ResponseEntity<FlashcardResponse> createFlashcard(@Valid @RequestBody FlashcardRequest request){
        FlashcardResponse response = flashcardService.createFlashcard(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // DELETE mapping to delete a single flashcard by ID
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteFlashcard(@Valid @RequestBody FlashcardRequest request){
        flashcardService.deleteFlashcard(request);
        return ResponseEntity.status(HttpStatus.OK).body("Successfully deleted flashcard with id " + request.id() + " for user id" + request.requestUserId());
    }
    //  PUT mapping to update either the front or back of a flashcard
    @PutMapping("/change")
    public ResponseEntity<FlashcardResponse> updateFlashcard(@Valid @RequestBody FlashcardRequest request){
        FlashcardResponse response = flashcardService.updateFlashcard(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // USE A ReviewRequest DTO AS THE INPUT IN THE BODY WHICH TRIGGERS
    // AN SM-2 CALCULATION THROUGH THE SpacedRepetitionService
    @PatchMapping("/{flashcardId}/review")
    public ResponseEntity<FlashcardResponse> reviewNote(@Valid @RequestBody ReviewRequest reviewContent, @PathVariable Long flashcardId){
        FlashcardResponse response = flashcardService.reviewFlashcard(flashcardId,reviewContent);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
