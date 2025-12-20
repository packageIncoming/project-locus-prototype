package com.locus.projectlocusprototype.Note;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ReviewRequest(
        @Min(0) @Max(5)  int qualityScore,
        Long userId
) {
}
