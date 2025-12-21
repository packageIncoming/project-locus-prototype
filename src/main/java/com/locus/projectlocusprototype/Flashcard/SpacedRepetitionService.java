package com.locus.projectlocusprototype.Flashcard;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SpacedRepetitionService {
    // This SpacedRepetitionService uses the SM-2 Algorithm to calculate when a note should be reviewed
    // For a good explanation, visit https://github.com/thyagoluciano/sm2


    public void judgeFlashcard(Flashcard flashcard, Integer quality){
        if (quality < 3){
            // user failed, reset interval AND repetitions
            flashcard.setRepetitions(0);
            flashcard.setInterval(1);
            // update review date to tomorrow
            flashcard.setNextReviewDate(LocalDateTime.now().plusDays(1));
        } else {
            // if this was the first pass (i.e., note.getRepetitions() == 0) then set interval to 1 (review tmr)
            // if this was the second pass, set Interval to 6
            // Otherwise, set interval to (previous interval) * (ease factor)
            if (flashcard.getRepetitions() == 0) {
                // first pass
                flashcard.setRepetitions(1);
                flashcard.setInterval(1);
            } else if (flashcard.getRepetitions() == 1){
                // second pass
                flashcard.setRepetitions(flashcard.getRepetitions()+1);
                flashcard.setInterval(6);
            } else {
                // 2+ pass
                flashcard.setRepetitions(flashcard.getRepetitions()+1);
                flashcard.setInterval((int) Math.ceil(flashcard.getInterval() * flashcard.getEaseFactor()));
            }
            flashcard.setNextReviewDate(flashcard.getNextReviewDate().plusDays(flashcard.getInterval()));

        }
        // update ease factor (equation from SM-2 algorithm description)
        double newEF = flashcard.getEaseFactor() + (0.1 - (5 - quality) * (.08 + (5-quality) * .02));
        if (newEF < 1.3){
            newEF = 1.3; // minimum value for Easing Factor
        }
        flashcard.setEaseFactor(newEF);
        //NOTE: SpacedRepetitionService does NOT save the changes to the note.
    }
}
