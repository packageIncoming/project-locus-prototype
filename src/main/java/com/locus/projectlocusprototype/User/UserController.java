package com.locus.projectlocusprototype.User;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // POST method to create a user
    @PostMapping
    public void createUser(@RequestBody User user){
        userService.createUser(user);
    }
}
