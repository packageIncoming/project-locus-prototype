package com.locus.projectlocusprototype.Flashcard;


public record FlashcardRequest(
        Long noteId,
        String front,
        String back
) {}
