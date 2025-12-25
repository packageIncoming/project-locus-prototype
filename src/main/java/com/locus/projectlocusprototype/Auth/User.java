package com.locus.projectlocusprototype.Auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @Size(min=5)
    private String username;

    @NotNull
    @Getter
    @Setter
    @JsonIgnore
    private String password;


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
