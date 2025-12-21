package com.locus.projectlocusprototype.Note;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    // GET for a singular note
    @GetMapping(path = "/{noteId}")
    public NoteResponse one(@PathVariable Long noteId, @Valid @RequestBody NoteRequest noteRequest){
        return noteService.getNote(noteId,noteRequest);
    }

    // GET for all notes for a user
    @GetMapping("/usernotes/{userId}")
    public List<Note> userNotes(@PathVariable Long userId){
        return noteService.getAllNotesForUser(userId);
    }
    // POST endpoint to create a note
    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<NoteResponse> makeNote(@Valid  @RequestBody NoteRequest note){
        NoteResponse response = noteService.createNote(note);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    //PUT endpoint to update a note's title OR content
    @PutMapping("/{noteId}")
    public ResponseEntity<NoteResponse> updateNote(@PathVariable Long noteId, @Valid @RequestBody NoteRequest noteContent){
        NoteResponse response = noteService.updateNote(noteId,noteContent);
        return ResponseEntity.status(HttpStatus.OK).body(response);

    }

    //DELETE endpoint to delete an existing note
    @DeleteMapping("/{noteId}")
    public ResponseEntity<String> deleteNote(@PathVariable Long noteId, @Valid @RequestBody NoteRequest noteRequest){
        noteService.deleteNote(noteId,noteRequest);
        return ResponseEntity.status(HttpStatus.OK).body("Successfully deleted note with id " + noteId);
    }

//    //TODO: IMPLEMENT PATCH /api/notes/{noteId}/review ENDPOINT TO PROCESS REVIEWING
//    // USE A ReviewRequest DTO AS THE INPUT IN THE BODY WHICH TRIGGERS
//    // AN SM-2 CALCULATION THROUGH THE SpacedRepetitionService
//    @PatchMapping("/{noteId}/review")
//    public ResponseEntity<NoteResponse> reviewNote(@PathVariable Long noteId, @Valid @RequestBody ReviewRequest reviewContent){
//        NoteResponse response = noteService.reviewNote(noteId,reviewContent);
//        return ResponseEntity.status(HttpStatus.OK).body(response);
//    }

}
