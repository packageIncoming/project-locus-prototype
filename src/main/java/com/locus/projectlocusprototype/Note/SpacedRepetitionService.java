package com.locus.projectlocusprototype.Note;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SpacedRepetitionService {
    // This SpacedRepetitionService uses the SM-2 Algorithm to calculate when a note should be reviewed
    // For a good explanation, visit https://github.com/thyagoluciano/sm2


    public void judgeNote(Note note, Integer quality){
        if (quality < 3){
            // user failed, reset interval AND repetitions
            note.setRepetitions(0);
            note.setInterval(1);
            // update review date to tomorrow
            note.setNextReviewDate(LocalDateTime.now().plusDays(1));
        } else {
            // if this was the first pass (i.e., note.getRepetitions() == 0) then set interval to 1 (review tmr)
            // if this was the second pass, set Interval to 6
            // Otherwise, set interval to (previous interval) * (ease factor)
            if (note.getRepetitions() == 0) {
                // first pass
                note.setRepetitions(1);
                note.setInterval(1);
            } else if (note.getRepetitions() == 1){
                // second pass
                note.setRepetitions(note.getRepetitions()+1);
                note.setInterval(6);
            } else {
                // 2+ pass
                note.setRepetitions(note.getRepetitions()+1);
                note.setInterval((int) Math.ceil(note.getInterval() * note.getEaseFactor()));
            }
            note.setNextReviewDate(note.getNextReviewDate().plusDays(note.getInterval()));

        }
        // update ease factor (equation from SM-2 algorithm description)
        double newEF = note.getEaseFactor() + (0.1 - (5 - quality) * (.08 + (5-quality) * .02));
        if (newEF < 1.3){
            newEF = 1.3; // minimum value for Easing Factor
        }
        note.setEaseFactor(newEF);
        //NOTE: SpacedRepetitionService does NOT save the changes to the note.
    }
}
