package com.locus.projectlocusprototype.AI;

public record AIRequest(
        Long noteId,
        Long userId,
        Integer count
) {
}
