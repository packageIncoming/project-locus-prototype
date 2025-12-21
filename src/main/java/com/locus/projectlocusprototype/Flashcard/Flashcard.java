package com.locus.projectlocusprototype.Flashcard;

import com.locus.projectlocusprototype.User.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name="flashcards")
public class Flashcard {
    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE)
    @SequenceGenerator(name = "flashcards_id",sequenceName = "flashcards_id",allocationSize = 1)
    @Getter
    private Long id;

    @Getter
    @Setter
    @NotEmpty
    private String front;

    @Getter
    @Setter
    @NotEmpty
    private String back;

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

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="user_id",nullable = false)
    @Getter
    @Setter
    private User user;

    public Flashcard(String front, String back, Double easeFactor, Integer interval, LocalDateTime nextReviewDate, Integer repetitions, User user) {
        this.front = front;
        this.back = back;
        this.easeFactor = 2.5;
        this.interval=0;
        this.repetitions = 0;
        this.nextReviewDate = LocalDateTime.now();
        this.user = user;
    }


    public Flashcard() {
        this.easeFactor = 2.5;
        this.interval=0;
        this.repetitions = 0;
        this.nextReviewDate = LocalDateTime.now();
    }
}
