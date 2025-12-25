package com.locus.projectlocusprototype.Auth;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // POST method to create a user
    @PostMapping("/register")
    public ResponseEntity<String> createUser(@Valid @RequestBody UserRequest request){
        authService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("Successfully created user " + request.username());
    }

    //  POST method to log in a user returns a JWT
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(@RequestBody UserRequest request) {
        AuthResponse response = authService.processLoginRequest(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
