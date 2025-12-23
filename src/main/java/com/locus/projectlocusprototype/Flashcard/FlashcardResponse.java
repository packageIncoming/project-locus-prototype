package com.locus.projectlocusprototype.Flashcard;

import java.time.LocalDateTime;

public record FlashcardResponse(
        Long id,
        String front,
        String back,
        Double easeFactor,
        Integer interval,
        LocalDateTime nextReviewDate,
        Integer repetitions,
        String associatedNote,
        Long associatedUserId
) {
}
