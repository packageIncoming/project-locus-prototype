package com.locus.projectlocusprototype.Note;

import com.locus.projectlocusprototype.Exceptions.InvalidNoteRequestException;
import com.locus.projectlocusprototype.Exceptions.ResourceNotFoundException;
import com.locus.projectlocusprototype.Auth.User;
import com.locus.projectlocusprototype.Auth.AuthService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NoteService {
    private final NoteRepository noteRepository;
    private final AuthService authService;

    public NoteService(NoteRepository noteRepository, AuthService authService) {
        this.noteRepository = noteRepository;
        this.authService = authService;
    }

    //  PRIVATE METHODS

    private NoteResponse noteToResponse(Note note){
        return new NoteResponse(
                note.getId(),
                note.getTitle(),
                note.getContent(),
                note.getUser().getUsername(),
                note.getCreated()
        );
    }

    private void validateNoteRequest(NoteRequest request) throws InvalidNoteRequestException{
        //  Validate the inputs
        if (request.content().isEmpty() || request.content().length() < 10){
            throw new InvalidNoteRequestException("ERROR: Length of request.content must be at least 10 characters");
        }
        if (request.title().isEmpty() ){
            throw new InvalidNoteRequestException("ERROR: Request.title cannot be empty");
        }
    }



    //  PUBLIC METHODS

    // Retrieves a single note by ID for a given user (through Authentication object)
    public NoteResponse getNote(Long noteId, Authentication authentication) {
        //  Get the note (if it exists)
        Note note = noteRepository.findNoteById(noteId).orElseThrow(
                ()-> new ResourceNotFoundException("Note with ID " + noteId + " does not exist")
        );

        //  Make sure the note actually exists
        Object details = authentication.getPrincipal();
        if (details instanceof UserDetails){
            if (!((UserDetails) details).getUsername().equals(note.getUser().getUsername())){
                //  Obscure the existence of the note from unauthenticated requests
                throw new ResourceNotFoundException("Note with ID " + noteId + " does not exist");
            }
        }
        return noteToResponse(note);
    }

    // Creates a single note for a given user (through Authentication object)
    public NoteResponse createNote(NoteRequest request, Authentication authentication){
        validateNoteRequest(request);
        User user = authService.getUserFromAuthenticationObject(authentication);

        //  Check if the user already has a note with the title and raise an InvalidNoteRequestException if so
        if (noteRepository.findExistingNoteForUserWithTitle(user, request.title()).isPresent()) {
            throw new InvalidNoteRequestException("ERROR: Note with title " + request.title() + " already exists for this user");
        }

        //  Now we know the user exists, is authenticated, and they don't have an existing note with this title
        //  -> Create the note
        Note note = new Note(request.title(),request.content(),user);
        noteRepository.save(note);
        return noteToResponse(note);
    }

    // UPDATES a single note for a given user (through Authentication object)
    public NoteResponse updateNote(Long noteId, NoteRequest request, Authentication authentication) {

        //  Get the user
        User user =  authService.getUserFromAuthenticationObject(authentication);
        Note note =  noteRepository.getReferenceById(noteId);

        //  Check if this user owns this note:
        if (!note.getUser().equals(user)) {
            //  Obfuscate note existence from user that doesn't own the note
            throw new ResourceNotFoundException("Note with ID " + noteId + " does not exist");
        }

        //  Now we know the user owns the note, so apply and save the changes then return the response
        if ((request.content() != null) && (!request.content().isBlank())){
            note.setContent(request.content());

        }
        if ((request.title() != null) &&( !request.title().isBlank())){
            note.setTitle(request.title());

        }
        noteRepository.save(note);
        return noteToResponse(note);


    }

    //  Delete a specific note for a user by Authentication object
    public void deleteNote(Long noteId,Authentication authentication) {
        //  Get the user for this request
        User user = authService.getUserFromAuthenticationObject(authentication);

        //  Get this note (throw an exception if it doesn't exist)
        Note note = noteRepository.findNoteById(noteId).orElseThrow(
                ()->new ResourceNotFoundException("ERROR: Note with id" + noteId + " does not exist"));

        //  Check if this user owns this note
        if (note.getUser()!= user){
            //Obfuscate note existence
            throw new ResourceNotFoundException("ERROR: Note with id" + noteId + " does not exist");
        }

        //Now we know the note exists and the user owns it, so we can delete it
        noteRepository.delete(note);
    }

    //  Get all the notes for a user by Authentication object
    public List<NoteResponse> getAllNotesForUser(Authentication authentication) {
        User user = authService.getUserFromAuthenticationObject(authentication);
        return noteRepository.getNotesByUser(user).stream().map(this::noteToResponse).toList();
    }

}
