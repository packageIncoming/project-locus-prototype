package com.locus.projectlocusprototype.Note;

import jakarta.validation.constraints.Size;

public record NoteRequest (
        String title,
        @Size(min=10) String content
){
}
