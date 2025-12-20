package com.locus.projectlocusprototype.Note;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public record NoteResponse(
        Long id,
        String title,
        String content,
        String authorName,
        Timestamp createdAt,
        LocalDateTime nextReviewDate
) {
}
