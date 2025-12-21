package com.locus.projectlocusprototype.Note;

import java.sql.Timestamp;

public record NoteResponse(
        Long id,
        String title,
        String content,
        String authorName,
        Timestamp createdAt
) {
}
