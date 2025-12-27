package com.locus.projectlocusprototype.Flashcard;

import com.locus.projectlocusprototype.Exceptions.InvalidFlashcardRequestException;
import com.locus.projectlocusprototype.Exceptions.ResourceNotFoundException;
import com.locus.projectlocusprototype.Note.*;
import com.locus.projectlocusprototype.Auth.User;
import com.locus.projectlocusprototype.Auth.AuthService;
import jakarta.validation.Valid;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FlashcardService {
    private final FlashcardRepository flashcardRepository;
    private final SpacedRepetitionService spacedRepetitionService;
    private final AuthService authService;
    private final NoteRepository noteRepository;


    // CONSTRUCTORS
    public FlashcardService(FlashcardRepository flashcardRepository, SpacedRepetitionService spacedRepetitionService, AuthService authService, NoteRepository noteRepository) {
        this.flashcardRepository = flashcardRepository;
        this.spacedRepetitionService = spacedRepetitionService;
        this.authService = authService;
        this.noteRepository = noteRepository;
    }

    //  PRIVATE METHODS
    private Flashcard getFlashcardForUserByFlashcardId(User user, Long flashcardId){
        //  Get the flashcard
        Flashcard flashcard = flashcardRepository.findFlashcardById(flashcardId).orElseThrow(
                ()-> new InvalidFlashcardRequestException("ERROR: Flashcard with ID " + flashcardId + " does not exist"));

        //  Check if this user owns this flashcard
        if (!flashcard.getUser().equals(user)){
            throw new InvalidFlashcardRequestException("ERROR: Flashcard with ID " + flashcardId + " does not exist");
        } else {
            return flashcard;
        }
    }

    //  PUBLIC METHODS
    public FlashcardResponse flashcardToResponse(@NonNull Flashcard flashcard){
        return new FlashcardResponse(
                flashcard.getId(),
                flashcard.getFront(),
                flashcard.getBack(),
                flashcard.getEaseFactor(),
                flashcard.getInterval(),
                flashcard.getNextReviewDate(),
                flashcard.getRepetitions(),
                flashcard.getNote().getTitle(),
                flashcard.getUser().getId()
        );
    }

    // Get a single flashcard for a user via Authentication object
    public FlashcardResponse getFlashcard(Long flashcardId, Authentication authentication){

        //  Get the user from the authentication
        User user = authService.getUserFromAuthenticationObject(authentication);
        return flashcardToResponse(getFlashcardForUserByFlashcardId(user,flashcardId));
    }

    //  Get all flashcards for a single note
    public List<FlashcardResponse> getFlashcardsForNote(Long noteId, Authentication authentication) {
        //  Get the note
        Note note = noteRepository.findNoteById(noteId).orElseThrow(
                ()->new ResourceNotFoundException("ERROR: Note with id" + noteId + " does not exist")
        );

        //  Get the user
        User user = authService.getUserFromAuthenticationObject(authentication);

        //  Check if this user owns this note, and if so return all flashcards that belong to this note
        if(note.getUser().equals(user)){
            return flashcardRepository.findFlashcardsByNote(note).stream().map(
                    this::flashcardToResponse
            ).toList();
        } else {
            throw new ResourceNotFoundException("ERROR: Note with id" + noteId + " does not exist");
        }
    }

    //  Get all flashcards for a user through the authentication object
    public List<FlashcardResponse> getFlashcardsForUser(Authentication authentication) {
        User user = authService.getUserFromAuthenticationObject(authentication);
        return flashcardRepository.findFlashcardsByUser(user).stream().map(this::flashcardToResponse).toList();
    }

    //  Create a single flashcard for a user
    public FlashcardResponse createFlashcard(@NonNull FlashcardRequest request, Authentication authentication){
        //  Get the user
        User user = authService.getUserFromAuthenticationObject(authentication);
        //  Get the note
        Note note = noteRepository.findNoteById(request.noteId()).orElseThrow(
                ()-> new InvalidFlashcardRequestException("ERROR: Note with ID " + request.noteId() + " does not exist"));
        // Check if note belongs to this user
        if(!note.getUser().equals(user)){
            throw new InvalidFlashcardRequestException("ERROR: Note with ID " + request.noteId() + " does not exist");
        }
        //  Create the flashcard
        Flashcard flashcard = new Flashcard(note,user,request.back(),request.front());
        flashcardRepository.save(flashcard);

        //  Add the flashcard to its associated note's flashcards hashset
        note.getFlashcards().add(flashcard);
        noteRepository.save(note);

        //  Return the response
        return flashcardToResponse(flashcard);

    }

    //  Delete a single flashcard for a user by flashcard ID
    public void deleteFlashcard(Long flashcardId, Authentication authentication) {
        //  Get the user
        User user = authService.getUserFromAuthenticationObject(authentication);

        //  Get the flashcard
        Flashcard flashcard = getFlashcardForUserByFlashcardId(user,flashcardId);
        //  Delete the flashcard
        flashcardRepository.delete(flashcard);
    }

    //  Update the front and/or the back of a flashcard
    public FlashcardResponse updateFlashcard(Long flashcardId, @Valid FlashcardRequest request, Authentication authentication) {
        //  Get user
        User user  = authService.getUserFromAuthenticationObject(authentication);

        //  Get flashcard
        Flashcard flashcard = getFlashcardForUserByFlashcardId(user,flashcardId);

        //  Update based on presence of inputs
        if (request.front() != null && !request.front().isEmpty()){
            flashcard.setFront(request.front());
        }
        if (request.back() != null  && !request.back().isEmpty()){
            flashcard.setBack(request.back());
        }
        flashcardRepository.save(flashcard);
        return flashcardToResponse(flashcard);
    }

    //  Review a flashcard using the SpacedRepetitionService
    public FlashcardResponse reviewFlashcard(Long flashcardId, ReviewRequest reviewContent,Authentication authentication) {
        //  Get the user
        User user = authService.getUserFromAuthenticationObject(authentication);

        //  Get the flashcard
        Flashcard flashcard = getFlashcardForUserByFlashcardId(user,flashcardId);

        // now we know the flashcard exists AND the user is correct, review the note
        spacedRepetitionService.judgeFlashcard(flashcard,reviewContent.qualityScore());

        // now save the result and return the response
        flashcardRepository.save(flashcard);

        return flashcardToResponse(flashcard);
    }

}
