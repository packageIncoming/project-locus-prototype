package com.locus.projectlocusprototype.Note;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NoteRequest (
        @NotBlank String title,
        @Size(min=10) String content,
        Long userId
){
}
