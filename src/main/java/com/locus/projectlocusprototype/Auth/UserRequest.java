package com.locus.projectlocusprototype.Auth;

import jakarta.validation.constraints.NotEmpty;

public record UserRequest(
        @NotEmpty
        String username,
        @NotEmpty
        String password,
        String email
) {
}
