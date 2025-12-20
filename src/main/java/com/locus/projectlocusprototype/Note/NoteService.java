package com.locus.projectlocusprototype.Note;

import com.locus.projectlocusprototype.Exceptions.ResourceNotFoundException;
import com.locus.projectlocusprototype.User.User;
import com.locus.projectlocusprototype.User.UserRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class NoteService {
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final SpacedRepetitionService spacedRepetitionService;

    public NoteService(NoteRepository noteRepository, UserRepository userRepository, SpacedRepetitionService spacedRepetitionService) {
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
        this.spacedRepetitionService = spacedRepetitionService;
    }

    private NoteResponse noteToResponse(Note note){
        return new NoteResponse(
                note.getId(),
                note.getTitle(),
                note.getContent(),
                note.getUser().getUsername(),
                note.getCreated(),
                note.getNextReviewDate()
        );
    }

    // TODO: Modify to use a requesterId to make sure the right user is accessing the note
    public NoteResponse getNote(Long noteId,NoteRequest noteContent) {
        Optional<Note> note =  noteRepository.findNoteById(noteId);
        if (note.isPresent()){
            if (!Objects.equals(noteContent.userId(), note.get().getUser().getId())) {
                // Throw a NotFound rather than Access Denied in order to hide existing data from malicious/malformed requests
                throw new ResourceNotFoundException("ERROR: Note with id " + noteId + " does not exist; cannot be updated");
            }
            return noteToResponse(note.get());
        }  else {
            throw new ResourceNotFoundException("ERROR: no note exists with id " + noteId);
        }
    }

    public NoteResponse createNote(NoteRequest request) {
        // check to make sure request's content length is at least 10 characters
        if (request.content().length() < 10){
            throw new ResourceNotFoundException("ERROR: Content of request must be at least 10 characters");
        }
        // get the user by userId in the request
        Optional<User> user = userRepository.findUserById(request.userId());
        if (user.isEmpty()) {
            throw new ResourceNotFoundException("ERROR: User with id " + request.userId() + " does not exist");
        }
        Note note = new Note(
                request.title(),
                request.content(),
                user.get()
        );
        noteRepository.save(note);
        return noteToResponse(note);
    }
    public NoteResponse updateNote(Long noteId, NoteRequest noteContent) {
        //make sure note exists
        Optional<Note> note = noteRepository.findNoteById(noteId);
        if (note.isEmpty()) {
            throw new ResourceNotFoundException("ERROR: Note with id " + noteId + " does not exist; cannot be updated");
        }
        Note obj = note.get();
        // check if noteContent has title and/or content
        if (!noteContent.title().isBlank()){
            obj.setTitle(noteContent.title());
        }
        if(!noteContent.content().isBlank() && noteContent.content().length()>=10){
            obj.setContent(noteContent.content());
        }
        if (!Objects.equals(noteContent.userId(), obj.getUser().getId())) {
            // Throw a NotFound rather than Access Denied in order to hide existing data from malicious/malformed requests
            throw new ResourceNotFoundException("ERROR: Note with id " + noteId + " does not exist; cannot be updated");
        }
        noteRepository.save(obj);
        return noteToResponse(obj);
    }
    public void deleteNote(Long noteId, NoteRequest noteContent) {
        Optional<Note> note = noteRepository.findNoteById(noteId);
        if (note.isEmpty()) {
            throw new ResourceNotFoundException("ERROR: Note with id " + noteId + " does not exist; cannot be deleted");
        }
        Note obj = note.get();
        if (!Objects.equals(noteContent.userId(), obj.getUser().getId())) {
            // Throw a NotFound rather than Access Denied in order to hide existing data from malicious/malformed requests
            throw new ResourceNotFoundException("ERROR: Note with id " + noteId + " does not exist; cannot be updated");
        }
        noteRepository.delete(obj);
    }

    public List<Note> getAllNotesForUser(Long userId) {
        Optional<User> user = userRepository.findUserById(userId);
        if(user.isEmpty()){
            throw new ResourceNotFoundException("ERROR: User with id " + userId + " does not exist");
        }
        return noteRepository.getNotesByUser(user.get());


    }

    public NoteResponse reviewNote(Long noteId, ReviewRequest reviewContent) {
        // check to make sure note exists
        // check to make sure the user in reviewContent has access to this note
        // If either the user does not have access OR the note does not exist, return a 404
        //  the first case returns a 404 to prevent malicious requests from finding other users' content
        Optional<Note> noteFind= noteRepository.findNoteById(noteId);
        if (noteFind.isEmpty()){
            throw new ResourceNotFoundException("ERROR: no note exists with id " + noteId);
        }
        Note note = noteFind.get();
        // now we know the note exists, check if the user is correct
        if (!Objects.equals(reviewContent.userId(), note.getUser().getId())){
            throw new ResourceNotFoundException("ERROR: no note exists with id " + noteId);
        }
        // now we know the note exists AND the user is correct, review the note
        spacedRepetitionService.judgeNote(note,reviewContent.qualityScore());
        // now save the result and return the response
        noteRepository.save(note);
        return noteToResponse(note);
    }
}
