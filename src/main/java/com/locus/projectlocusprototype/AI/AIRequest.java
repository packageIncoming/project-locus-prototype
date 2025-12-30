package com.locus.projectlocusprototype.AI;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record AIRequest(
        Long noteId,
        @Max(12) @Min(1) Integer count
) {
}
