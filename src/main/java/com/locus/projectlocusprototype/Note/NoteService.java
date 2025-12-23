package com.locus.projectlocusprototype.Note;

import com.locus.projectlocusprototype.Exceptions.ResourceNotFoundException;
import com.locus.projectlocusprototype.Flashcard.FlashcardService;
import com.locus.projectlocusprototype.User.User;
import com.locus.projectlocusprototype.User.UserRepository;
import com.locus.projectlocusprototype.User.UserService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class NoteService {
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public NoteService(NoteRepository noteRepository, UserRepository userRepository, FlashcardService flashcardService, UserService userService) {
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
        this.userService = userService;
    }

    private NoteResponse noteToResponse(Note note){
        return new NoteResponse(
                note.getId(),
                note.getTitle(),
                note.getContent(),
                note.getUser().getUsername(),
                note.getCreated()
        );
    }

    public NoteResponse getNote(Long noteId,NoteRequest noteContent) {
        userService.validateNoteRequest(noteId,noteContent);
        Note note =  noteRepository.getReferenceById(noteId);
        return noteToResponse(note);
    }

    public NoteResponse getNote(Long noteId,Long userId) {
        NoteRequest noteContent = new NoteRequest("temporary value", "temporary value", userId);
        userService.validateNoteRequest(noteId,noteContent);
        Note note =  noteRepository.getReferenceById(noteId);
        return noteToResponse(note);
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
        userService.validateNoteRequest(noteId,noteContent);
        Note obj =  noteRepository.getReferenceById(noteId);
        // check if noteContent has title and/or content
        if (!noteContent.title().isBlank()){
            obj.setTitle(noteContent.title());
        }
        if(!noteContent.content().isBlank() && noteContent.content().length()>=10){
            obj.setContent(noteContent.content());
        }

        noteRepository.save(obj);
        return noteToResponse(obj);
    }
    public void deleteNote(Long noteId, NoteRequest noteContent) {
        userService.validateNoteRequest(noteId,noteContent);
        Note obj =  noteRepository.getReferenceById(noteId);
        noteRepository.delete(obj);
    }

    public List<Note> getAllNotesForUser(Long userId) {
        Optional<User> user = userRepository.findUserById(userId);
        if(user.isEmpty()){
            throw new ResourceNotFoundException("ERROR: User with id " + userId + " does not exist");
        }
        return noteRepository.getNotesByUser(user.get());


    }


}
