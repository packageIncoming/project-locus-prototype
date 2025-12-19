package com.locus.projectlocusprototype.User;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE)
    @SequenceGenerator(name="userIdSequence",sequenceName = "userIdSequence",allocationSize = 1)
    @Getter
    private Long id;

    @NotNull
    @Getter
    @Setter
    private String username;

    @NotNull
    @Getter
    @Setter
    private String email;

    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }

    public User() {

    }


}
