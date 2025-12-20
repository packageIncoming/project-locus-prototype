package com.locus.projectlocusprototype.User;

import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    public void createUser(User user) {
        Optional<User> u = userRepository.findUserByUsername(user.getUsername());
        if(u.isPresent()){
            throw new IllegalStateException("User with username: " + user.getUsername() + " already exists");
        }
        userRepository.save(user);
    }
}
