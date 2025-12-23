package com.locus.projectlocusprototype.Note;

import com.locus.projectlocusprototype.User.User;
import com.locus.projectlocusprototype.Flashcard.Flashcard;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="notes")
public class Note {

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE)
    @SequenceGenerator(name = "notes_id",sequenceName = "notes_id",allocationSize = 1)
    @Getter
    private Long id;

    @Getter
    @Setter
    @NotEmpty
    private String title;

    @Getter
    @Setter
    @Column(length=1000)
    private String content;



    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="user_id",nullable = false)
    @Getter
    @Setter
    private User user;

    @CreationTimestamp
    @Getter
    private Timestamp created;

    @Getter
    @OneToMany(mappedBy = "note", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Flashcard> flashcards = new HashSet<>();

    public Note(String title, String content, User user) {
        this.title = title;
        this.content = content;
        this.user = user;

    }

    public Note() {

    }


    @Override
    public String toString() {
        return "Note{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", created=" + created +
                '}';
    }
}
