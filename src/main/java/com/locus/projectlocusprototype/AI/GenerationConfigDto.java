package com.locus.projectlocusprototype.AI;

public record GenerationConfigDto(
        String response_mime_type,
        ResponseSchemaDto responseSchema
) {
}
