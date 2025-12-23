package com.locus.projectlocusprototype.AI;

public record GeminiRequest(
        int cardCount,
        String topicTitle,
        String content
) {}