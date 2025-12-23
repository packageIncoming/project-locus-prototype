package com.locus.projectlocusprototype.User;

import com.locus.projectlocusprototype.Exceptions.ResourceNotFoundException;
import com.locus.projectlocusprototype.Flashcard.Flashcard;
import com.locus.projectlocusprototype.Flashcard.FlashcardRepository;
import com.locus.projectlocusprototype.Flashcard.FlashcardRequest;
import com.locus.projectlocusprototype.Note.Note;
import com.locus.projectlocusprototype.Note.NoteRepository;
import com.locus.projectlocusprototype.Note.NoteRequest;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final NoteRepository noteRepository;
    private final FlashcardRepository flashcardRepository;

    public UserService(UserRepository userRepository, NoteRepository noteRepository, FlashcardRepository flashcardRepository) {
        this.userRepository = userRepository;
        this.noteRepository = noteRepository;
        this.flashcardRepository = flashcardRepository;
    }


    public void createUser(User user) {
        Optional<User> u = userRepository.findUserByUsername(user.getUsername());
        if(u.isPresent()){
            throw new IllegalStateException("User with username: " + user.getUsername() + " already exists");
        }
        userRepository.save(user);
    }

    public void validateUserExists(Long userId) throws ResourceNotFoundException{
        // simple check to see if a user with userId exists
        Optional<User> u = userRepository.findUserById(userId);
        if (u.isEmpty()){
            throw new ResourceNotFoundException("ERROR: User with id " + userId + " does not exist");
        }
    }

    public void validateNoteRequest(Long noteId, NoteRequest request) throws ResourceNotFoundException {
        // This method checks to see if the Note with noteId belongs to the userId in request,
        // and if not it throws a ResourceNotFoundException
        // If the note does not exist it will also throw a ResourceNotFoundException
        Optional<Note> n = noteRepository.findNoteById(noteId);
        if(n.isEmpty()){
            throw new ResourceNotFoundException("ERROR: Note with id " + noteId + " does not exist");
        }
        Note note = n.get();
        if (!Objects.equals(note.getUser().getId(), request.userId())) {
            // Throw a NotFound rather than Access Denied in order to hide existing data from malicious/malformed requests
            throw new ResourceNotFoundException("ERROR: Note with id " + noteId + " does not exist");
        }
    }

    public void validateFlashcardRequest(Long flashcardId, FlashcardRequest request) throws ResourceNotFoundException {
        // This method checks to see if the Flashcard with flashcardId belongs to the userId in request,
        // and if not it throws a ResourceNotFoundException
        // If the flashcard does not exist it will also throw a ResourceNotFoundException
        Optional<Flashcard> f = flashcardRepository.findFlashcardById(flashcardId);
        if(f.isEmpty()){
            throw new ResourceNotFoundException("ERROR: Flashcard with id " + flashcardId + " does not exist");
        }
        Flashcard flashcard = f.get();
        if (!Objects.equals(flashcard.getUser().getId(), request.requestUserId())) {
            // Throw a NotFound rather than Access Denied in order to hide existing data from malicious/malformed requests
            throw new ResourceNotFoundException("ERROR: Flashcard with id " + flashcardId + " does not exist");
        }
    }
}
