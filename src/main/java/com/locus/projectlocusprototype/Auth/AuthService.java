package com.locus.projectlocusprototype.Auth;

import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, @Lazy AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public User getUserFromAuthenticationObject(Authentication authentication) throws UsernameNotFoundException {
        //  Get the user by looking up their username in the user repository, throws a UsernameNotFoundException
        Object principal = authentication.getPrincipal();
        assert principal instanceof UserDetails;
        UserDetails userDetails = (UserDetails) principal;
        return userRepository.findUserByUsername(userDetails.getUsername()).orElseThrow(
                ()-> new UsernameNotFoundException("ERROR: User with username " + userDetails.getUsername() + " not found")
        );
    }

    public void createUser(UserRequest request){
        Optional<User> u = userRepository.findUserByUsername(request.username());
        if(u.isPresent()){
            throw new IllegalStateException("User with username: " + request.username() + " already exists");
        }
        User user = new User();
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setEmail(request.email());
        userRepository.save(user);
    }



    public AuthResponse processLoginRequest(UserRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(),request.password())
        );
        String jwt  = jwtService.generateToken(request.username());
        return new AuthResponse(jwt);
    }
}
