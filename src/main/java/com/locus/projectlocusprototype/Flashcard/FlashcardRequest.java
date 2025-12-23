package com.locus.projectlocusprototype.Flashcard;

import jakarta.validation.constraints.NotNull;

public record FlashcardRequest(
        Long id,
        @NotNull
        Long requestUserId,
        Long noteId,
        String front,
        String back


) {}
