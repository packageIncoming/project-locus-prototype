package com.locus.projectlocusprototype.Note;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    //  GET for a singular note
    @GetMapping(path = "/{noteId}")
    public NoteResponse one(@PathVariable Long noteId, Authentication authentication){
        return noteService.getNote(noteId,authentication);
    }

    //  GET for all notes for a user
    @GetMapping("/usernotes")
    public List<NoteResponse> userNotes(Authentication authentication){
        return noteService.getAllNotesForUser(authentication);
    }

    //  POST endpoint to create a note
    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<NoteResponse> makeNote(@Valid  @RequestBody NoteRequest note, Authentication authentication){
        NoteResponse response = noteService.createNote(note,authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    //  PUT endpoint to update a note's title OR content
    @PutMapping("/{noteId}")
    public ResponseEntity<NoteResponse> updateNote(
            @PathVariable Long noteId,
            @Valid @RequestBody NoteRequest noteContent,
            Authentication authentication){
        NoteResponse response = noteService.updateNote(noteId,noteContent,authentication);
        return ResponseEntity.status(HttpStatus.OK).body(response);

    }

    //  DELETE endpoint to delete an existing note
    @DeleteMapping("/{noteId}")
    public ResponseEntity<String> deleteNote(@PathVariable Long noteId, Authentication authentication){
        noteService.deleteNote(noteId,authentication);
        return ResponseEntity.status(HttpStatus.OK).body("Successfully deleted note with id " + noteId);
    }



}
