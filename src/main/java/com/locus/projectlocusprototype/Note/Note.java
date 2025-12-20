package com.locus.projectlocusprototype.Note;

import com.locus.projectlocusprototype.User.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;
import java.time.LocalDateTime;

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



    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="user_id",nullable = false)
    @Getter
    @Setter
    private User user;

    @CreationTimestamp
    @Getter
    private Timestamp created;

    //review-related attributes:
    @Getter
    @Setter
    private Double easeFactor; // EaseFactor used in SM-2 calculation
    @Getter
    @Setter
    private Integer interval; // Interval of days until next review (Used in SM-2 Calculation)
    @Getter
    @Setter
    private LocalDateTime nextReviewDate; // The actual localized date of when the user will next review
    @Getter
    @Setter
    private Integer repetitions; // The number of successful repetitions the user has had (fail sets to 0)


    public Note(String title, String content, User user) {
        this.title = title;
        this.content = content;
        this.user = user;
        this.easeFactor = 2.5;
        this.interval=0;
        this.repetitions = 0;
        this.nextReviewDate = LocalDateTime.now();
    }

    public Note() {
        this.easeFactor = 2.5;
        this.interval=0;
        this.repetitions = 0;
        this.nextReviewDate = LocalDateTime.now();
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
