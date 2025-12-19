package com.locus.projectlocusprototype.Note;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

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
    private String content;

    @Getter
    @Setter
    @NotNull
    private Long associatedUser;

    @CreationTimestamp
    @Getter
    private Timestamp created;


    public Note(String title, String content, Long associatedUser) {
        this.title = title;
        this.content = content;
        this.associatedUser = associatedUser;
    }

    public Note() {

    }

    @Override
    public String toString() {
        return "Note{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", associatedUser=" + associatedUser +
                ", created=" + created +
                '}';
    }
}
