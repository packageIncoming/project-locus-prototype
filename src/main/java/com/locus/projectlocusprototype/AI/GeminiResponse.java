package com.locus.projectlocusprototype.AI;

import java.util.List;

// This matches the actual JSON structure returned by Google
public record GeminiResponse(List<Candidate> candidates) {
    public record Candidate(Content content) {}
    public record Content(List<Part> parts) {}
    public record Part(String text) {}

    public String extractText() {
        if (candidates != null && !candidates.isEmpty()) {
            return candidates.get(0).content().parts().get(0).text();
        }
        return "";
    }
}