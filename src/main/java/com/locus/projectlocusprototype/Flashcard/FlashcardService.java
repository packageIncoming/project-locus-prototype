package com.locus.projectlocusprototype.Flashcard;

import com.locus.projectlocusprototype.Exceptions.ResourceNotFoundException;
import com.locus.projectlocusprototype.Note.Note;
import com.locus.projectlocusprototype.Note.NoteRepository;
import com.locus.projectlocusprototype.Note.NoteRequest;
import com.locus.projectlocusprototype.User.User;
import com.locus.projectlocusprototype.User.UserRepository;
import com.locus.projectlocusprototype.User.UserService;
import jakarta.validation.Valid;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class FlashcardService {
    private final FlashcardRepository flashcardRepository;
    private final SpacedRepetitionService spacedRepetitionService;
    private final UserRepository userRepository;
    private final UserService userService;
    private final NoteRepository noteRepository;


    // CONSTRUCTORS
    public FlashcardService(FlashcardRepository flashcardRepository, SpacedRepetitionService spacedRepetitionService, UserRepository userRepository, UserService userService, NoteRepository noteRepository) {
        this.flashcardRepository = flashcardRepository;
        this.spacedRepetitionService = spacedRepetitionService;
        this.userRepository = userRepository;
        this.userService = userService;
        this.noteRepository = noteRepository;
    }

    //  PRIVATE METHODS
    private Flashcard  userFlashcardGet(FlashcardRequest request) throws ResourceNotFoundException{
        // This method 'safely' gets a flashcard from the repository
        // through a FlashcardRequest by checking if the flashcard ID
        // is specified, if a flashcard exists with that ID, if the
        // user ID is specified and that user exists, and finally if that user owns that
        // particular flashcard. If any of these are wrong it throws a
        // ResourceNotFound exception, and if not it returns the flashcard

        // make sure 'id' is given in request
        if (request.id().describeConstable().isEmpty()){
            throw new ResourceNotFoundException("ERROR: Flashcard with id " + request.id() + " does not exist");
        }
        // check if a flashcard with the given id exists
        Optional<Flashcard> f = flashcardRepository.findFlashcardById(request.id());
        if (f.isEmpty()){
            throw new ResourceNotFoundException("ERROR: Flashcard with id " + request.id() + " does not exist");
        }
        // make sure the user exists
        userService.validateUserExists(request.requestUserId());
        // get the user and flashcard
        User user = userRepository.getReferenceById(request.requestUserId());
        Flashcard flashcard = flashcardRepository.getReferenceById(request.id());
        // make sure this user owns this flashcard
        if (!Objects.equals(flashcard.getUser(),user)){
            throw new ResourceNotFoundException("ERROR: Flashcard with id " + request.id() + " does not exist");
        }
        return flashcard;
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

    public FlashcardResponse getFlashcard(Long id, @NonNull FlashcardRequest request){
        Optional<Flashcard> f = flashcardRepository.findFlashcardById(id);
        Optional<User> u = userRepository.findUserById(request.requestUserId());
        // handle not finding flashcard
        if (f.isEmpty()){
            throw new ResourceNotFoundException("ERROR: no flashcard exists with id " + id);
        }
        Flashcard flashcard = f.get();

        // handle not finding user OR invalid user
        if (u.isEmpty() || !Objects.equals(request.requestUserId(), flashcard.getUser().getId())){
            throw new ResourceNotFoundException("ERROR: no flashcard exists with id " + id);
        }

        // at this point: flashcard exists and correct user is requesting
        return flashcardToResponse(flashcard);

    }

    public FlashcardResponse reviewFlashcard(Long flashcardId, ReviewRequest reviewContent) {
        // check to make sure flashcard exists
        // check to make sure the user in reviewContent has access to this flashcard
        // If either the user does not have access OR the note does not exist, return a 404
        //  the first case returns a 404 to prevent malicious requests from finding other users' content
        Optional<Flashcard> flashcardFind= flashcardRepository.findFlashcardById(flashcardId);
        if (flashcardFind.isEmpty()){
            throw new ResourceNotFoundException("ERROR: no flashcard exists with id " + flashcardId);
        }
        Flashcard flashcard = flashcardFind.get();
        // now we know the flashcard exists, check if the user is correct
        if (!Objects.equals(reviewContent.userId(), flashcard.getUser().getId())){
            throw new ResourceNotFoundException("ERROR: no flashcard exists with id " + flashcardId);
        }
        // now we know the flashcard exists AND the user is correct, review the note
        spacedRepetitionService.judgeFlashcard(flashcard,reviewContent.qualityScore());
        // now save the result and return the response
        flashcardRepository.save(flashcard);
        return flashcardToResponse(flashcard);
    }


    public List<FlashcardResponse> getFlashcardsForNote(Long noteId, @Valid NoteRequest request) {
        // first make sure this user has the ability to access this note
        userService.validateNoteRequest(noteId,request);
        Note note = noteRepository.getReferenceById(noteId);
        List<Flashcard> flashcards = flashcardRepository.findFlashcardsByNote(note);
        // convert flashcards to flashcard response objects
        List<FlashcardResponse> response = new ArrayList<>();
        for (Flashcard f: flashcards){
            response.add(flashcardToResponse(f));
        }
        return response;
    }

    public List<FlashcardResponse> getFlashcardsForUser(Long userId) {
        userService.validateUserExists(userId);
        User user = userRepository.getReferenceById(userId);
        List<Flashcard> flashcards = flashcardRepository.findFlashcardsByUser(user);
        // convert flashcards to flashcard response objects
        List<FlashcardResponse> response = new ArrayList<>();
        for (Flashcard f: flashcards){
            response.add(flashcardToResponse(f));
        }
        return response;
    }

    public FlashcardResponse createFlashcard(@NonNull FlashcardRequest request){
        // in order for a flashcard to be made, the requestUserId, noteId, front, and back must exist
        // check if any of these are missing and throw an exception if so:
        // (requestUserId is already required)
        if (  request.front().isEmpty() || request.back().isEmpty()){
            throw new RuntimeException("ERROR: Flashcard needs a 'front' and 'back' String input");
        }
        if (request.noteId().describeConstable().isEmpty()) {
            throw new RuntimeException("ERROR: Flashcard needs a 'noteId' long input; flashcards are tied to Note objects");
        }

        // make sure the user exists
        userService.validateUserExists(request.requestUserId());
        // make sure the note exists
        userService.validateNoteRequest(request.noteId(),new NoteRequest("temp","temp", request.requestUserId()));
        // now we know the user exists, the note exists, the note belongs to the user,
        // and the flashcard data is valid
        // begin making the Flashcard object
        Note note = noteRepository.getReferenceById(request.noteId());
        User user = userRepository.getReferenceById(request.requestUserId());

        Flashcard flashcard = new Flashcard(
                note,
                user,
                request.back(),
                request.front()
        );
        flashcardRepository.save(flashcard);
        return flashcardToResponse(flashcard);
    }




    public void deleteFlashcard(@Valid @NonNull FlashcardRequest request) {
        Flashcard flashcard = userFlashcardGet(request);
        // now delete
        flashcardRepository.delete(flashcard);
    }

    public FlashcardResponse updateFlashcard(@Valid FlashcardRequest request) {
        Flashcard flashcard = userFlashcardGet(request);
        if (!request.front().isEmpty()){
            flashcard.setFront(request.front());
        }
        if (!request.back().isEmpty()){
            flashcard.setBack(request.back());
        }
        return flashcardToResponse(flashcard);
    }
}
